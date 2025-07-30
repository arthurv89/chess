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
import nl.arthurvlug.chess.engine.ace.board.AceBoardDebugUtils.string
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils
import nl.arthurvlug.chess.utils.LogUtils
import nl.arthurvlug.chess.utils.MoveUtils
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

    fun startThinking(engineBoard: ACEBoard): Observable<Move?> {
        this.currentEngineBoard = engineBoard
        return startThinking()
    }

    fun startThinking(): Observable<Move?> {
        return Observable.create { sub: Subscriber<in Move?> ->
            this.subscriber =
                sub
            Thread {
                try {
                    think()
                    sub.onCompleted()
                } catch (e: Exception) {
                    sub.onError(e)
                }
            }.start()
        }
    }

    private fun think() {
        info("New thinking process")
        newIncomingState = Optional.empty()
        cutoffs = 0
        nodesEvaluated = 0
        hashHits = 0


        pv = PrincipalVariation()
        var startDepth = 0
        try {
            depthNow = startDepth
            while (depthNow <= depth) {
                thinkingEngineBoard = currentEngineBoard.cloneBoard()
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
                val bestMove = alphaBetaRoot(depthNow)
                if (bestMove == null) {
                    err("No best move")
                    emitNewMove(null)
                    return
                }
                if (depthNow == depth) {
                    applyAndEmitMove(bestMove)
                    return
                }
                if (depthNow > 1000) {
                    err("depth is extremely high")
                    return
                }
                info(String.format("Done thinking %d moves.", depthNow))
                depthNow++
            }
            info(String.format("Done thinking ALL %d moves.", depth))
        } catch (e: NewEngineBoardException) {
            // TODO: Where is newIncomingEngineBoard set?
            thinkingEngineBoard = newIncomingEngineBoard.get()
            newIncomingEngineBoard = Optional.empty()
            pv = PrincipalVariation()
        } catch (e: OpponentMoveCameInException) {
            err("Our turn!")
            val incomingState = newIncomingState.get()
            newIncomingState = Optional.empty()

            val move = incomingState.move
            if (move != null) {
                val newMove = MoveUtils.toEngineMove(move)
                err("New move was: $newMove")

                if (pv!!.pvHead == UnapplyableMoveUtils.createMove(newMove, currentEngineBoard)) {
                    System.arraycopy(pv!!.rawLine, 1, pv!!.rawLine, 0, pv!!.rawLine.size - 1)
                    startDepth = depthNow - 1
                } else {
                    pv = PrincipalVariation()
                    startDepth = 1
                }

                info("Applying $newMove")
                currentEngineBoard.apply(newMove)
            }

            maxThinkingTime = thinkingTime(incomingState.thinkingParams)
            info("Set max thinking time to $maxThinkingTime")
            timer = Stopwatch.createStarted()
            iterator = 0
        } catch (e: OutOfThinkingTimeException) {
            // Out of time: just play the move
            val move = pv!!.pvHead
            System.arraycopy(pv!!.rawLine, 1, pv!!.rawLine, 0, pv!!.rawLine.size - 1)
            applyAndEmitMove(move)
        }
    }

    fun alphaBetaRoot(depth: Int): Int? {
        if (depth == 0) {
            return calculateScore(thinkingEngineBoard)
        }

        val generatedMoves: List<Int>
        try {
            generatedMoves = thinkingEngineBoard.generateMoves()
        } catch (e: KingEatingException) {
            return null
        }

        //		final List<Integer> generatedMoves = Lists.newArrayList(priorityMove.get());
        var alpha = OTHER_PLAYER_WINS * 2
        val beta = CURRENT_PLAYER_WINS
        var bestMove: Int? = null
        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        val line = PrincipalVariation()

        var score = Int.MIN_VALUE

        val pvMove = pv!!.getMoveAtHeight(0)
        if (pvMove != PrincipalVariation.NO_MOVE) {
            swapPvMove(generatedMoves, pvMove)
        }
        LogUtils.logDebug("Root: Moves after []: %s".formatted(movesToString(generatedMoves)))
        for (move in generatedMoves) {
            LogUtils.logDebug(
                "Root: Start Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(
                        move
                    ), score, alpha, pv
                )
            )
            var newHeight: Int? = null
            if (move == generatedMoves[0]) {
                newHeight = 1
            }

            //			info("Investigating " + UnapplyableMoveUtils.toString(move));
//			final int fiftyMove = thinkingEngineBoard.getFiftyMove();
            val blackOccupiedSquaresBefore =
                BitboardUtils.toString(thinkingEngineBoard.occupiedSquares[ColorUtils.BLACK.toInt()])
            thinkingEngineBoard.apply(move)
            try {
                thinkingEngineBoard.generateMoves()
            } catch (e: KingEatingException) {
                // The applied move is not valid. Ignore
                LogUtils.logDebug(
                    "Root: The applied move %s is not valid. Ignore".formatted(
                        UnapplyableMoveUtils.toShortString(
                            move
                        )
                    )
                )
                thinkingEngineBoard.unapply(
                    move,
                    white_king_or_rook_queen_side_moved,
                    white_king_or_rook_king_side_moved,
                    black_king_or_rook_queen_side_moved,
                    black_king_or_rook_king_side_moved
                )
                val blackOccupiedSquaresAfter =
                    BitboardUtils.toString(thinkingEngineBoard.occupiedSquares[ColorUtils.BLACK.toInt()])
                if (blackOccupiedSquaresAfter != blackOccupiedSquaresBefore) {
                    throw RuntimeException("Uh oh!")
                }
                continue
            }
            // Do a recursive search
            val `val` = -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, 1, ImmutableList.of(move))
            //			debugMoveStack(val);
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved
            )
            val blackOccupiedSquaresAfter =
                BitboardUtils.toString(thinkingEngineBoard.occupiedSquares[ColorUtils.BLACK.toInt()])
            if (blackOccupiedSquaresAfter != blackOccupiedSquaresBefore) {
                throw RuntimeException("Uh oh!")
            }

            score = max(`val`.toDouble(), score.toDouble()).toInt()
            if (score > alpha) {
                updatePv(pv!!, line, move)
                if (cutoffEnabled && score >= beta) {
                    cutoffs++
                    LogUtils.logDebug(String.format("[ABPruning Root] Best move score: %d", score))
                    return move
                }
                alpha = score
                bestMove = move
            }
            LogUtils.logDebug(
                "Root: End Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(
                        move
                    ), score, alpha, pv
                )
            )
        }
        info("[ABPruning Root] Best move score: $alpha")
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
        val indent = toIndent(movesPlayed)

        LogUtils.logDebug("To move: " + thinkingEngineBoard.toMove, indent)
        //		logDebug("AlphaBeta. Depth=%s".formatted(depth), indent);
        performBrakeActions()

        //		if (thinkingEngineBoard.getFiftyMove() >= 50 || thinkingEngineBoard.getRepeatedMove() >= 3) {
//			logDebug("50 move / repeated move", indent);
//			return 0;
//		}
        var hashf = TranspositionTable.hashfALPHA
        val zobristHash = thinkingEngineBoard.zobristHash
        val hashElement = transpositionTable[zobristHash]
        if (hashElement != null) {
            hashHits++
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
            LogUtils.logDebug("Stopping because the player can now take the king", indent)
            return CURRENT_PLAYER_WINS - height
        }

        var bestMove: Int? = null
        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        var score = alpha
        if (pvHeight != null) {
            val pvMove = pline.getMoveAtHeight(pvHeight)
            if (pvMove != PrincipalVariation.NO_MOVE) {
                swapPvMove(generatedMoves, pvMove)
            }
        }

        LogUtils.logDebug(
            "Moves after []: %s - %s".formatted(
                movesToString(movesPlayed),
                movesToString(generatedMoves)
            ), indent
        )
        var hasValidMove = false
        //		final int fiftyMove = thinkingEngineBoard.getFiftyMove();
        for (move in generatedMoves) {
            LogUtils.logDebug(
                "Start Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(move),
                    score,
                    alpha,
                    pv
                ), indent
            )
            // Do a recursive search
            val dumpBeforeApply = ACEBoardUtils.stringDump(thinkingEngineBoard)
            val thinkingEngineBoardBefore: String = thinkingEngineBoard.string()
            thinkingEngineBoard.apply(move)

            var newHeight: Int? = null
            if (pvHeight != null && move == generatedMoves.first()) {
                newHeight = pvHeight + 1
            }
            val newMovesPlayed = if (MoveUtils.DEBUG) {
                ImmutableList.builder<Int>().addAll(movesPlayed).add(move).build()
            } else {
                ImmutableList.of()
            }
            val recursiveVal = -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, height + 1, newMovesPlayed)
            if (!isLost(recursiveVal)) {
                hasValidMove = true
            }

            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved
            )
            val dumpAfterUnapply = ACEBoardUtils.stringDump(thinkingEngineBoard)
            val thinkingEngineBoardAfter: String = thinkingEngineBoard.string()
            if (dumpAfterUnapply != dumpBeforeApply) {
                throw RuntimeException("Uh oh!")
            }

            score = max(score.toDouble(), recursiveVal.toDouble()).toInt()
            if (score > alpha) {
                if (cutoffEnabled && score >= beta) {
                    cutoffs++
                    transpositionTable[depth, score, TranspositionTable.hashfBETA, move] =
                        zobristHash
                    LogUtils.logDebug("BETA CUT OFF. $score >= $beta", indent)
                    return score
                }
                updatePv(pline, line, move)
                bestMove = move
                alpha = score
                hashf = TranspositionTable.hashfEXACT
            }
            LogUtils.logDebug(
                "End Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(move),
                    score,
                    alpha,
                    pv
                ), indent
            )
        }

        if (!hasValidMove) {
//			thinkingEngineBoard.toMove = opponent(thinkingEngineBoard.toMove);
//			thinkingEngineBoard.apply(bestMove);
//			boolean isCheckmate = thinkingEngineBoard.canTakeKing();
//			if(isCheckmate) {
//				return score;
//			}

            thinkingEngineBoard.toMove = ColorUtils.opponent(thinkingEngineBoard.toMove)
            thinkingEngineBoard.mutateGeneralBoardOccupation()
            //			thinkingEngineBoard.apply(bestMove);
            val opponentCanTakeKing = thinkingEngineBoard.canTakeKing()

            //			thinkingEngineBoard.unapply(bestMove,
//					thinkingEngineBoard.white_king_or_rook_queen_side_moved,
//					thinkingEngineBoard.white_king_or_rook_king_side_moved,
//					thinkingEngineBoard.black_king_or_rook_queen_side_moved,
//					thinkingEngineBoard.black_king_or_rook_king_side_moved,
//					thinkingEngineBoard.getFiftyMoveClock());
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
        val indent = toIndent(movesPlayed)
        performBrakeActions()
        var score = calculateScore(thinkingEngineBoard)
        eventBus!!.post(ThinkEvent(iterator))
        if (depth == 0 || !quiesceEnabled) {
            return score
        }

        if (score >= beta) {
            LogUtils.logDebug("Beta cutoff", indent)
            return score
        }
        if (score > alpha) {
            LogUtils.logDebug("Alpha cutoff", indent)
            alpha = score
        }

        val takeMoves: List<Int>
        try {
            takeMoves = thinkingEngineBoard.generateTakeMoves()
        } catch (e: KingEatingException) {
            LogUtils.logDebug("Eating king", indent)
            return CURRENT_PLAYER_WINS - height
        }

        val white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        for (move in takeMoves) {
//			final int fiftyMove = thinkingEngineBoard.getFiftyMove();
            val blackOccupiedSquaresBefore =
                BitboardUtils.toString(thinkingEngineBoard.occupiedSquares[ColorUtils.BLACK.toInt()])
            thinkingEngineBoard.apply(move)
            val `val` = -quiesceSearch(-beta, -alpha, depth - 1, height + 1, movesPlayed)
            //			debugMoveStack(val);
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved
            )
            val blackOccupiedSquaresAfter =
                BitboardUtils.toString(thinkingEngineBoard.occupiedSquares[ColorUtils.BLACK.toInt()])
            if (blackOccupiedSquaresAfter != blackOccupiedSquaresBefore) {
                throw RuntimeException("Uh oh!")
            }

            //			debugMoveStack("Evaluating board\n{}Score: {}\n", thinkingEngineBoard.string(), val);
            score = max(score.toDouble(), `val`.toDouble()).toInt()
            if (score > alpha) {
                if (cutoffEnabled && `val` >= beta) {
                    // Beta cut-off
                    cutoffs++
                    LogUtils.logDebug("Beta cut-off", indent)
                    return beta
                }

                alpha = score
            }
            LogUtils.logDebug(
                "End Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(move),
                    score,
                    alpha,
                    pv
                ), indent
            )
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

        info("Applying after emitting: " + UnapplyableMoveUtils.toString(unapplyableMove))
        currentEngineBoard.apply(unapplyableMove)

        // Start thinking about the next move
        // Emit the move that we found
        info("Emitting move $bestMove")
        emitNewMove(bestMove)
    }

    private fun err(s: String) {
        log.error(this.name + " > " + s)
    }

    private fun emitNewMove(move: Move?) {
        info("Emitting new move: $move")
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

        //		info(String.format("Time spent %d/%d ms", timer.elapsed(TimeUnit.MILLISECONDS), maxThinkingTime));
        // TODO: Wrap the incoming events into an IncomingEvent object and check here whether is Optional.empty or not
        if (newIncomingEngineBoard.isPresent) {
            err("New incoming engineboard: $newIncomingEngineBoard")
            throw NewEngineBoardException()
        } else if (newIncomingState.isPresent) {
            err("Opponent move came in: $newIncomingState")
            throw OpponentMoveCameInException()
        } else {
            val moveAtHeight = pv!!.getMoveAtHeight(0)
            if (moveAtHeight != null && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime) {
                err("Out of time. Playing move " + UnapplyableMoveUtils.toString(moveAtHeight) + " at depth " + (depthNow - 1))
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
        nodesEvaluated++
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

    private fun findPrioPosition(generatedMoves: List<Int>, prioMove: Int, i: Int): Stream<Int> {
        if (generatedMoves[i] == prioMove) {
            return Stream.of(i)
        }
        return Stream.empty()
    }

    private fun shouldPause(): Boolean {
        return moveListContainsAll("f1e1", "g7g6")
    }

    fun setDepth(depth: Int) {
        this.depth = depth
    }

    fun disableQuiesce() {
        this.quiesceEnabled = false
    }

    fun useSimplePieceEvaluator() {
        this.evaluator = SimplePieceEvaluator()
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setEventBus(eventBus: EventBus?) {
        this.eventBus = eventBus
    }

    fun debugBreakpoint(): Boolean {
        return ACEBoardUtils.stringDump(thinkingEngineBoard).contains("......♚♜")
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
}
