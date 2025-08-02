package nl.arthurvlug.chess.engine.ace.alphabeta

import com.google.common.base.Preconditions
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import lombok.Getter
import nl.arthurvlug.chess.engine.ColorUtils
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.IncomingState
import nl.arthurvlug.chess.engine.ace.KingEatingException
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils
import nl.arthurvlug.chess.engine.ace.board.ACEBoard
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams
import nl.arthurvlug.chess.utils.LogUtils.logDebug
import nl.arthurvlug.chess.utils.MoveUtils
import nl.arthurvlug.chess.utils.MoveUtils.DEBUG
import nl.arthurvlug.chess.utils.MoveUtils.LOAD_TEST
import nl.arthurvlug.chess.utils.ThinkEvent
import nl.arthurvlug.chess.utils.board.FieldUtils
import nl.arthurvlug.chess.utils.board.pieces.PieceType
import nl.arthurvlug.chess.utils.game.Move
import nl.arthurvlug.chess.utils.slf4j
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.observers.Observers
import java.util.Arrays
import java.util.Collections
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.concurrent.Volatile
import kotlin.math.max

class AlphaBetaPruningAlgorithm @JvmOverloads constructor(
    configuration: AceConfiguration,
    initialACEBoard: ACEBoard
) {
    private val log by slf4j

    private var currentEngineBoard: ACEBoard
    private var maxThinkingTime = Int.MAX_VALUE
    private var timer: Stopwatch = Stopwatch.createUnstarted()

    @Getter
    private var nodesEvaluated = 0

    @Getter
    private var cutoffs = 0

    @Getter
    private var hashHits = 0

    var evaluator: BoardEvaluator
    private val quiesceMaxDepth: Int
    @JvmField
	var depth: Int = Int.MAX_VALUE
    private var quiesceEnabled = true

    @JvmField
	var thinkingEngineBoard: ACEBoard

    @Volatile
    private var newIncomingState = Optional.empty<IncomingState>()

    @Volatile
    private var newIncomingEngineBoard = Optional.empty<ACEBoard>()

    private val incomingMoves: Observer<IncomingState>
    private var name: String? = null
    private var eventBus: EventBus? = null
    private var iterator = 0
    private var subscriber: Subscriber<in Move?>? = null
    private var depthNow = 0
    lateinit var pv: PrincipalVariation

    @JvmField
	var cutoffEnabled: Boolean = true

    init {
        this.evaluator = configuration.evaluator
        this.quiesceMaxDepth = configuration.quiesceMaxDepth
        this.depth = configuration.searchDepth

        Preconditions.checkArgument(depth > 0)

        this.currentEngineBoard = initialACEBoard
        this.thinkingEngineBoard = currentEngineBoard.cloneBoard()
        this.incomingMoves = Observers.create { incomingState: IncomingState ->
            newIncomingState = Optional.of(incomingState)
        }
    }

    fun startThinking(infinite: Boolean): Observable<Move?> {
        return Observable.create { sub: Subscriber<in Move?> ->
            this.subscriber = sub
            Thread {
                try {
                    think(infinite)
                    sub.onCompleted()
                } catch (e: Exception) {
                    sub.onError(e)
                }
            }.start()
        }
    }

    private fun think(infinite: Boolean) {
        while(true) {
            thinkAndEmit()
            if(!infinite) {
                return
            }
        }
    }

    private fun thinkAndEmit() {
        if(DEBUG) {
            info("New thinking process")
        }
        newIncomingState = Optional.empty()
        if(LOAD_TEST) {
            cutoffs = 0
            nodesEvaluated = 0
            hashHits = 0
        }


        pv = PrincipalVariation()
        var startDepth = 0
        try {
            depthNow = startDepth
            while (depthNow <= depth) {
                thinkingEngineBoard = currentEngineBoard.cloneBoard()
                if(DEBUG) {
                    info(
                        String.format(
                            "Start thinking for %d ms on depth %d. PV line: %s",
                            maxThinkingTime,
                            depthNow,
                            Arrays.stream(pv.line).boxed().filter { x: Int -> x != PrincipalVariation.NO_MOVE }
                                .map { m: Int? ->
                                    UnapplyableMoveUtils.toShortString(
                                        m!!
                                    )
                                }.collect(Collectors.toList())
                        )
                    )
                }
                val bestMove = alphaBetaRoot(depthNow)
                if (bestMove == null) {
                    if(DEBUG) {
                        err("No best move")
                    }
                    emitNewMove(null)
                    return
                }
                if (depthNow == depth) {
                    applyAndEmitMove(bestMove)
                    return
                }
                if (depthNow > 1000) {
                    if(DEBUG) {
                        err("depth is extremely high")
                    }
                    return
                }
                if(DEBUG) {
                    info(String.format("Done thinking %d moves.", depthNow))
                }
                depthNow++
            }
            if(DEBUG) {
                info(String.format("Done thinking ALL %d moves.", depth))
            }
        } catch (e: NewEngineBoardException) {
            // TODO: Where is newIncomingEngineBoard set?
            thinkingEngineBoard = newIncomingEngineBoard.get()
            newIncomingEngineBoard = Optional.empty()
            pv = PrincipalVariation()
        } catch (e: OpponentMoveCameInException) {
            if(DEBUG) {
                err("Our turn!")
            }
            val incomingState = newIncomingState.get()
            newIncomingState = Optional.empty()

            val move = incomingState.move
            if (move != null) {
                val newMove = MoveUtils.toEngineMove(move)
                if(DEBUG) {
                    err("New move was: $newMove")
                }

                val iMove = UnapplyableMoveUtils.createMove(newMove, currentEngineBoard)
                if (pv.pvHead == iMove) {
                    System.arraycopy(pv.rawLine, 1, pv.rawLine, 0, pv.rawLine.size - 1)
                } else {
                    pv = PrincipalVariation()
                }

                if(DEBUG) {
                    info("Applying $newMove")
                }
                currentEngineBoard.apply(iMove)
            }

            maxThinkingTime = thinkingTime(incomingState.thinkingParams)
            if(DEBUG) {
                info("Set max thinking time to $maxThinkingTime")
            }
            timer = Stopwatch.createStarted()
            iterator = 0
        } catch (e: OutOfThinkingTimeException) {
            // Out of time: just play the move
            val move = pv.pvHead
            System.arraycopy(pv.rawLine, 1, pv.rawLine, 0, pv.rawLine.size - 1)
            applyAndEmitMove(move)
        }
    }

    fun alphaBetaRoot(depth: Int): Int? {
        DEBUG && breakpoint()
        if (depth == 0) {
            return calculateScore(thinkingEngineBoard)
        }

        val generatedMoves: List<Int>
        try {
            generatedMoves = thinkingEngineBoard.generateMoves()
        } catch (e: KingEatingException) {
            return null
        }
        DEBUG && breakpoint()

        //		final List<Integer> generatedMoves = Lists.newArrayList(priorityMove.get());
        var alpha = OTHER_PLAYER_WINS * 2
        val beta = CURRENT_PLAYER_WINS
        var bestMove: Int? = null
        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved
        val fiftyMove = thinkingEngineBoard.fiftyMove

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        val line = PrincipalVariation()

        var score = Int.MIN_VALUE

        val pvMove = pv!!.getMoveAtHeight(0)
        if (pvMove != PrincipalVariation.NO_MOVE) {
            swapPvMove(generatedMoves, pvMove)
        }
        DEBUG && breakpoint()
        if (DEBUG) {
            logDebug("Root: Moves after []: %s".formatted(movesToString(generatedMoves)))
        }
        for (move in generatedMoves) {
            DEBUG && breakpoint()
            if (DEBUG) {
                logDebug(
                    "Root: Start Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(
                            move
                        ), score, alpha, pv
                    )
                )
            }
            DEBUG && breakpoint()
            var newHeight: Int? = null
            if (move == generatedMoves[0]) {
                newHeight = 1
            }

            DEBUG && breakpoint()
            thinkingEngineBoard.apply(move)
            DEBUG && breakpoint()
            try {
                thinkingEngineBoard.generateMoves()
            } catch (e: KingEatingException) {
                // The applied move is not valid. Ignore
                if (DEBUG) {
                    logDebug(
                        "Root: The applied move %s is not valid. Ignore".formatted(
                            UnapplyableMoveUtils.toShortString(
                                move
                            )
                        )
                    )
                }
                DEBUG && breakpoint()
                thinkingEngineBoard.unapply(
                    move,
                    white_king_or_rook_queen_side_moved,
                    white_king_or_rook_king_side_moved,
                    black_king_or_rook_queen_side_moved,
                    black_king_or_rook_king_side_moved,
                    fiftyMove
                )
                DEBUG && breakpoint()
                continue
            }
            // Do a recursive search
            val recursionVal = -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, 1, ImmutableList.of(move))
            //			debugMoveStack(val);
            DEBUG && breakpoint()
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved,
                fiftyMove
            )
            DEBUG && breakpoint()

            score = max(recursionVal, score)
            if (score > alpha) {
                updatePv(pv, line, move)
                if (cutoffEnabled && score >= beta) {
                    if(LOAD_TEST) {
                        cutoffs++
                    }
                    if(DEBUG) {
                        logDebug(String.format("[ABPruning Root] Best move score: %d", score))
                    }
                    return move
                }
                alpha = score
                bestMove = move
            }
            if (DEBUG) {
                logDebug(
                "Root: End Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(
                        move
                    ), score, alpha, pv
                )
            )
                }
        }
        if(DEBUG) {
            info("[ABPruning Root] Best move score: $alpha")
        }
        return bestMove
    }

    private fun alphaBeta(
        alpha: Int,
        beta: Int,
        depth: Int,
        pline: PrincipalVariation,
        pvHeight: Int?,
        height: Int,
        movesPlayed: List<Int>
    ): Int {
        var alpha = alpha
        DEBUG && breakpoint()
        val indent = if(DEBUG) {
            toIndent(movesPlayed)
        } else {
            ""
        }

        if(DEBUG) {
            logDebug("To move: " + thinkingEngineBoard.toMove, indent)
        }
        performBrakeActions()

        var hashf = TranspositionTable.hashfALPHA
        val zobristHash = thinkingEngineBoard.zobristHash
        val hashElement = transpositionTable[zobristHash]
        if (hashElement != null) {
            if(LOAD_TEST) {
                hashHits++
            }
            if (hashElement.depth >= depth) {
                if (hashElement.flags == TranspositionTable.hashfEXACT) return hashElement.`val`
                if ((hashElement.flags == TranspositionTable.hashfALPHA) && (hashElement.`val` <= alpha)) return alpha
                if ((hashElement.flags == TranspositionTable.hashfBETA) && (hashElement.`val` >= beta)) return beta
            }
        }

        val line = PrincipalVariation()
        if (depth == 0) {
            // IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
            return quiesceSearch(alpha, beta, quiesceMaxDepth, height, movesPlayed)
        }

        val generatedMoves: List<Int>
        try {
            generatedMoves = thinkingEngineBoard.generateMoves()
        } catch (e: KingEatingException) {
            if(DEBUG) {
                logDebug("Stopping because the player can now take the king", indent)
            }
            return CURRENT_PLAYER_WINS - height
        }

        var bestMove: Int? = null
        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved
        val fiftyMove = thinkingEngineBoard.fiftyMove

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        var score = alpha
        if (pvHeight != null) {
            val pvMove = pline.getMoveAtHeight(pvHeight)
            if (pvMove != PrincipalVariation.NO_MOVE) {
                swapPvMove(generatedMoves, pvMove)
            }
        }

        if(DEBUG) {
            logDebug(
                "Moves after []: %s - %s".formatted(
                    movesToString(movesPlayed),
                    movesToString(generatedMoves)
                ), indent
            )
        }
        var hasValidMove = false
        for (move in generatedMoves) {
            if (DEBUG) {
                logDebug(
                    "Start Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(move),
                        score,
                        alpha,
                        pv
                    ), indent
                )
            }
            // Do a recursive search
            val dumpBeforeApply = ACEBoardUtils.stringDump(thinkingEngineBoard)
            DEBUG && breakpoint()
            thinkingEngineBoard.apply(move)
            DEBUG && breakpoint()

            var newHeight: Int? = null
            if (pvHeight != null && move == generatedMoves.first()) {
                newHeight = pvHeight + 1
            }
            val newMovesPlayed = if (MoveUtils.DEBUG) {
                ImmutableList.builder<Int>().addAll(movesPlayed).add(move).build()
            } else {
                ImmutableList.of()
            }
            DEBUG && breakpoint()
            val recursiveVal = -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, height + 1, newMovesPlayed)
            if (!isLost(recursiveVal)) {
                hasValidMove = true
            }

            DEBUG && breakpoint()
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved,
                fiftyMove
            )
            DEBUG && breakpoint()
            if(DEBUG) {
                val dumpAfterUnapply = ACEBoardUtils.stringDump(thinkingEngineBoard)
                if (dumpBeforeApply != dumpAfterUnapply) {
                    throw RuntimeException("Uh oh!")
                }
            }

            score = max(score, recursiveVal)
            if (score > alpha) {
                if (cutoffEnabled && score >= beta) {
                    if(LOAD_TEST) {
                        cutoffs++
                    }
                    transpositionTable[depth, score, TranspositionTable.hashfBETA, move] =
                        zobristHash
                    if(DEBUG) {
                        logDebug("BETA CUT OFF. $score >= $beta", indent)
                    }
                    return score
                }
                updatePv(pline, line, move)
                bestMove = move
                alpha = score
                hashf = TranspositionTable.hashfEXACT
            }
            if (DEBUG) {
                logDebug(
                    "End Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(move),
                        score,
                        alpha,
                        pv
                    ), indent
                )
            }
        }

        if (!hasValidMove) {
            thinkingEngineBoard.toMove = ColorUtils.opponent(thinkingEngineBoard.toMove)
            thinkingEngineBoard.mutateGeneralBoardOccupation()
            val opponentCanTakeKing = thinkingEngineBoard.canTakeKing()
            thinkingEngineBoard.toMove = ColorUtils.opponent(thinkingEngineBoard.toMove)

            if (!opponentCanTakeKing) {
                // Stalemate
                return 0
            }
        }

        transpositionTable[depth, alpha, hashf, bestMove] = zobristHash
        return score
    }

    private fun quiesceSearch(alpha: Int, beta: Int, depth: Int, height: Int, movesPlayed: List<Int>): Int {
        var alpha = alpha
        val indent = if(DEBUG) {
            toIndent(movesPlayed)
        } else {
            ""
        }
        performBrakeActions()
        var score = calculateScore(thinkingEngineBoard)
        eventBus!!.post(ThinkEvent(iterator))
        if (depth == 0 || !quiesceEnabled) {
            return score
        }

        if (score >= beta) {
            if(DEBUG) {
                logDebug("Beta cutoff", indent)
            }
            return score
        }
        if (score > alpha) {
            if(DEBUG) {
                logDebug("Alpha cutoff", indent)
            }
            alpha = score
        }

        val takeMoves: List<Int>
        try {
            takeMoves = thinkingEngineBoard.generateTakeMoves()
        } catch (e: KingEatingException) {
            if(DEBUG) {
                logDebug("Eating king", indent)
            }
            return CURRENT_PLAYER_WINS - height
        }

        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved
        val fiftyMove = thinkingEngineBoard.fiftyMove

        for (move in takeMoves) {
            thinkingEngineBoard.apply(move)
            val recursionValue = -quiesceSearch(-beta, -alpha, depth - 1, height + 1, movesPlayed)
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved,
                fiftyMove
            )
            score = max(score, recursionValue)
            if (score > alpha) {
                if (cutoffEnabled && recursionValue >= beta) {
                    // Beta cut-off
                    if(LOAD_TEST) {
                        cutoffs++
                    }
                    if(DEBUG) {
                        logDebug("Beta cut-off", indent)
                    }
                    return beta
                }

                alpha = score
            }
            if (DEBUG) {
                logDebug(
                    "End Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(move),
                        score,
                        alpha,
                        pv
                    ), indent
                )
            }
        }
        return score
    }

    private fun updatePv(pv: PrincipalVariation, line: PrincipalVariation, move: Int) {
        if (isWorsePv(pv, line, move)) {
            return
        }
        pv.rawLine[0] = move
        System.arraycopy(line.rawLine, 0, pv.rawLine, 1, line.rawLine.size - 1)
        pv.lineElements = line.lineElements + 1
    }

    private fun isWorsePv(pv: PrincipalVariation, line: PrincipalVariation, move: Int): Boolean {
        return pv.rawLine[0] == move
                && Arrays.copyOfRange(pv.line, 1, 1 + line.lineElements)
            .contentEquals(Arrays.copyOfRange(line.line, 0, line.lineElements))
    }

    private fun applyAndEmitMove(unapplyableMove: Int) {
        val bestMove = toMove(unapplyableMove)

        if(DEBUG) {
            info("Applying after emitting: " + UnapplyableMoveUtils.toString(unapplyableMove))
        }
        currentEngineBoard.apply(unapplyableMove)

        // Start thinking about the next move
        // Emit the move that we found
        if(DEBUG) {
            info("Emitting move $bestMove")
        }
        emitNewMove(bestMove)
    }

    private fun err(s: String) {
        log.error(this.name + " > " + s)
    }

    private fun emitNewMove(move: Move?) {
        if(DEBUG) {
            info("Emitting new move: $move")
        }
        this.maxThinkingTime = Int.MAX_VALUE
        iterator = 0
        subscriber!!.onNext(move)
    }

    private fun thinkingTime(thinkingParams: ThinkingParams): Int {
        val timeLeft = if (currentEngineBoard.toMove == ColorUtils.WHITE)
            thinkingParams.whiteTime
        else
            thinkingParams.blackTime

        if (currentEngineBoard.plyStack.size <= 1) {
            return 1000 // First second should take at most 1 second
        }
        //		double grandmasterThinkingTime = ThinkingTime.times[moveNumber];
//		int time = (int) (timeLeft * grandmasterThinkingTime);
//		return time;
        val timeDueToTimeLeft = (timeLeft - 2000 * thinkingEngineBoard.plyStack.size) / 50
        //		final int timeDueToInitialTime = initialClockTime / 50;
//		return Math.min(timeDueToInitialTime, timeDueToTimeLeft);
        return timeDueToTimeLeft
    }

    private fun performBrakeActions() {
        iterator++

        if ((iterator and 4095) != 0) {
            return
        }

        //		if(DEBUG) {info(String.format("Time spent %d/%d ms", timer.elapsed(TimeUnit.MILLISECONDS), maxThinkingTime));
        // TODO: Wrap the incoming events into an IncomingEvent object and check here whether is Optional.empty or not
        if (newIncomingEngineBoard.isPresent) {
            if(DEBUG) {
                err("New incoming engineboard: $newIncomingEngineBoard")
            }
            throw NewEngineBoardException()
        } else if (newIncomingState.isPresent) {
            if(DEBUG) {
                err("Opponent move came in: $newIncomingState")
            }
            throw OpponentMoveCameInException()
        } else {
            val moveAtHeight = pv!!.getMoveAtHeight(0)
            if (moveAtHeight != null && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime) {
                if(DEBUG) {
                    err("Out of time. Playing move " + UnapplyableMoveUtils.toString(moveAtHeight) + " at depth " + (depthNow - 1))
                }
                throw OutOfThinkingTimeException()
            }
        }
    }


    private fun losingScore(): Int {
        return OTHER_PLAYER_WINS - thinkingEngineBoard.plyStack.size
    }

    private fun info(s: String) {
        log.info(this.name + " -> " + s)
    }

    private fun moveListContainsAll(vararg moves: String): Boolean {
        val c = ImmutableList.copyOf(moves)
        return moveListStrings().containsAll(c)
    }

    private fun moveListStrings(): List<String> {
        return thinkingEngineBoard.plyStack.stream().map { m: Int? ->
            UnapplyableMoveUtils.toShortString(
                m!!
            )
        }.collect(Collectors.toList())
    }

    private fun calculateScore(board: ACEBoard): Int {
        if(LOAD_TEST) {
            nodesEvaluated++
        }
        val score = evaluator.evaluate(board)

        if (board.getToMove() == ColorUtils.BLACK.toInt()) {
            return -score
        }
        return score
    }

    private fun swapPvMove(moves: List<Int>, priorityMove: Int) {
        val idx = moves.indexOf(priorityMove)
        val moveShortString = UnapplyableMoveUtils.toShortString(priorityMove)
        Preconditions.checkArgument(idx != -1, "Could not find move %s in move list!".formatted(moveShortString))
        Collections.swap(moves, 0, idx)
    }

    fun setDepth(depth: Int) {
        this.depth = depth
    }

    fun disableQuiesce() {
        this.quiesceEnabled = false
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setEventBus(eventBus: EventBus?) {
        this.eventBus = eventBus
    }

    companion object {
        private const val CURRENT_PLAYER_WINS = 1000000000
        private const val OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS

        //	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
        private const val HASH_TABLE_LENGTH = 1048576 // Must be a power or 2

        private val transpositionTable = TranspositionTable(HASH_TABLE_LENGTH)
        private fun isLost(`val`: Int): Boolean {
            return `val` <= OTHER_PLAYER_WINS / 2
        }

        private fun movesToString(moves: List<Int>): String {
            return moves.stream().map { move: Int? -> UnapplyableMoveUtils.toShortString(move!!) }
                .collect(Collectors.joining(", "))
        }

        @JvmStatic
		fun toMove(unapplyableMove: Int): Move {
            val promotionType = promotionType(unapplyableMove)
            val bestMove = Move(
                FieldUtils.coordinates(UnapplyableMove.fromIdx(unapplyableMove).toInt()),
                FieldUtils.coordinates(UnapplyableMove.targetIdx(unapplyableMove).toInt()),
                promotionType
            )
            return bestMove
        }

        private fun promotionType(unapplyableMove: Int): Optional<PieceType> {
            val promotionPiece = UnapplyableMove.promotionPiece(unapplyableMove)
            if (promotionPiece == ColoredPieceType.NO_PIECE) {
                return Optional.empty()
            }
            return Optional.of(ColoredPieceType.from(promotionPiece).pieceType)
        }

        private fun toIndent(generatedMoves: List<Int>): String {
            return generatedMoves.stream().map { x: Int? -> "  " }.collect(Collectors.joining())
        }
    }

    fun getIncomingMoves(): Observer<IncomingState> {
        return incomingMoves;
    }

    fun getNodesEvaluated(): Int {
        return nodesEvaluated
    }

    fun getCutoffs(): Int {
        return cutoffs
    }

    private fun breakpoint(): Boolean {
//        return ACEBoardUtils.stringDump(thinkingEngineBoard).contains("......♚♜")
        return thinkingEngineBoard.breakpoint()
    }
}
