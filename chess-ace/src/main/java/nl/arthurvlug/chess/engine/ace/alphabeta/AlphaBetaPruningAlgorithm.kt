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
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove.fromIdx
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove.targetIdx
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams
import nl.arthurvlug.chess.utils.LogUtils.logDebug
import nl.arthurvlug.chess.utils.MoveUtils
import nl.arthurvlug.chess.utils.MoveUtils.ENGINE_DEBUG
import nl.arthurvlug.chess.utils.MoveUtils.LOAD_TEST
import nl.arthurvlug.chess.utils.MoveUtils.NON_ENGINE_DEBUG
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
import kotlin.concurrent.Volatile
import kotlin.math.max

class AlphaBetaPruningAlgorithm(
    configuration: AceConfiguration,
    initialACEBoard: ACEBoard
) {
    private val log by slf4j

    private var currentEngineBoard: ACEBoard
    private var maxThinkingTime = Int.MAX_VALUE
    private var timer: Stopwatch = Stopwatch.createUnstarted()

    val stats = LongArray(StatsType.values().size)

    private val killerMoves = Array(128) { IntArray(2) }
    val historyHeuristic = Array(64) { IntArray(64) } // from->to

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
        if(ENGINE_DEBUG) {
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
                if(ENGINE_DEBUG) {
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
                    if(ENGINE_DEBUG) {
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
                    if(ENGINE_DEBUG) {
                        err("depth is extremely high")
                    }
                    return
                }
                if(ENGINE_DEBUG) {
                    info(String.format("Done thinking %d moves.", depthNow))
                }
                depthNow++
            }
            if(ENGINE_DEBUG) {
                info(String.format("Done thinking ALL %d moves.", depth))
            }
        } catch (e: NewEngineBoardException) {
            // TODO: Where is newIncomingEngineBoard set?
            thinkingEngineBoard = newIncomingEngineBoard.get()
            newIncomingEngineBoard = Optional.empty()
            pv = PrincipalVariation()
        } catch (e: OpponentMoveCameInException) {
            if(NON_ENGINE_DEBUG) {
                info("Our turn!")
            }
            val incomingState = newIncomingState.get()
            newIncomingState = Optional.empty()

            val move = incomingState.move
            if (move != null) {
                val newMove = MoveUtils.toEngineMove(move)
                if(NON_ENGINE_DEBUG) {
                    err("New move was: $newMove")
                }

                val iMove = UnapplyableMoveUtils.createMove(newMove, currentEngineBoard)
                if (pv.pvHead == iMove) {
                    System.arraycopy(pv.rawLine, 1, pv.rawLine, 0, pv.rawLine.size - 1)
                } else {
                    pv = PrincipalVariation()
                }

                if(NON_ENGINE_DEBUG) {
                    info("Applying $newMove")
                }
                currentEngineBoard.apply(iMove)
            }

            maxThinkingTime = thinkingTime(incomingState.thinkingParams)
            if(NON_ENGINE_DEBUG) {
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
        ENGINE_DEBUG && breakpoint()
        if (depth == 0) {
            return calculateScore(thinkingEngineBoard)
        }

        val generatedMoves: List<Int>
        try {
            generatedMoves = profile(StatsType.MOVE_GEN) {
                thinkingEngineBoard.generateMoves()
            }
        } catch (e: KingEatingException) {
            return null
        }
        ENGINE_DEBUG && breakpoint()

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
        ENGINE_DEBUG && breakpoint()
//        if (DEBUG) {
//            logDebug("Root: Moves after []: %s".formatted(movesToString(generatedMoves)))
//        }
        for (move in generatedMoves) {
            ENGINE_DEBUG && breakpoint()
            if (ENGINE_DEBUG) {
                logDebug(
                    "Root: Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(move), score, alpha, pv
                    )
                )
            }
            ENGINE_DEBUG && breakpoint()
            var newHeight: Int? = null
            if (move == generatedMoves[0]) {
                newHeight = 1
            }

            ENGINE_DEBUG && breakpoint()
            profile(StatsType.APPLY) {
                thinkingEngineBoard.apply(move)
            }
            ENGINE_DEBUG && breakpoint()
            try {
                profile(StatsType.MOVE_GEN) {
                    thinkingEngineBoard.generateMoves()
                }
            } catch (e: KingEatingException) {
                // The applied move is not valid. Ignore
                if (NON_ENGINE_DEBUG) {
                    logDebug(
                        "Root: The applied move %s is not valid. Ignore".formatted(
                            UnapplyableMoveUtils.toShortString(move)
                        )
                    )
                }
                ENGINE_DEBUG && breakpoint()
                profile(StatsType.UNAPPLY) {
                    thinkingEngineBoard.unapply(
                        move = move,
                        white_king_or_rook_queen_side_moved_before = white_king_or_rook_queen_side_moved,
                        white_king_or_rook_king_side_moved_before = white_king_or_rook_king_side_moved,
                        black_king_or_rook_queen_side_moved_before = black_king_or_rook_queen_side_moved,
                        black_king_or_rook_king_side_moved_before = black_king_or_rook_king_side_moved,
                        fiftyMoveBefore = fiftyMove
                    )
                }
                ENGINE_DEBUG && breakpoint()
                continue
            }
            // Do a recursive search
            val recursionVal = profile(StatsType.ALPHA_BETA) {
                -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, 1, ImmutableList.of(move), 0)
            }
            //			debugMoveStack(val);
            ENGINE_DEBUG && breakpoint()
            profile(StatsType.UNAPPLY) {
                thinkingEngineBoard.unapply(
                    move,
                    white_king_or_rook_queen_side_moved,
                    white_king_or_rook_king_side_moved,
                    black_king_or_rook_queen_side_moved,
                    black_king_or_rook_king_side_moved,
                    fiftyMove
                )
            }
            ENGINE_DEBUG && breakpoint()

            score = max(recursionVal, score)
            if (score > alpha) {
                updatePv(pv, line, move)
                if (cutoffEnabled && score >= beta) {
                    if(LOAD_TEST) {
                        cutoffs++
                    }
                    updateKiller(0, move)
                    updateHistory(move, depth)
                    if(NON_ENGINE_DEBUG) {
                        logDebug(String.format("[ABPruning Root] Best move score: %d", score))
                    }
                    return move
                }
                alpha = score
                bestMove = move
            }
            if (NON_ENGINE_DEBUG) {
                logDebug("Root: End Move: %s, score=%d. PV: [%d] %s".formatted(
                    UnapplyableMoveUtils.toShortString(move), score, alpha, pv)
                )
            }
        }
        if(NON_ENGINE_DEBUG) {
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
        movesPlayed: List<Int>,
        ply: Int
    ): Int {
        ENGINE_DEBUG && breakpoint()

        if (depth == 0) {
            // IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
            return profile(StatsType.QUIESCE) {
                quiesceSearch(alpha, beta, quiesceMaxDepth, height, movesPlayed)
            }
        }

        var alpha = alpha
        val indent = if (ENGINE_DEBUG) {
            toIndent(movesPlayed)
        } else {
            ""
        }

        performBrakeActions()

        val zobristHash = thinkingEngineBoard.zobristHash
        val ttEntry = transpositionTable[zobristHash]
        ttEntry?.let {
            if (it.depth >= depth) {
                val value = it.value
                when (it.hashf) {
                    TranspositionTable.hashfEXACT ->
                        return value

                    TranspositionTable.hashfALPHA -> if (value <= alpha)
                        return alpha

                    TranspositionTable.hashfBETA -> if (value >= beta)
                        return beta
                }
            }
        }


        val line = PrincipalVariation()

        val generatedMoves: List<Int>
        try {
            generatedMoves = thinkingEngineBoard.generateMoves()
        } catch (e: KingEatingException) {
            if (ENGINE_DEBUG) {
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
        var hashf = TranspositionTable.hashfALPHA

        //		boolean incFiftyClock = thinkingEngineBoard.incFiftyClock;
        var score = alpha
        val pvMove = pline.getMoveAtHeight(height) ?: PrincipalVariation.NO_MOVE
        if (pvHeight != null) {
            if (pvMove != PrincipalVariation.NO_MOVE) {
                swapPvMove(generatedMoves, pvMove)
            }
        }
        val ttMove = ttEntry?.best
        profile(StatsType.ORDER_MOVES) {
            orderMoves(generatedMoves, ply, ttMove, pvMove)
        }

        //        if(DEBUG) {
        //            logDebug(
        //                "Moves after []: %s - %s".formatted(
        //                    movesToString(movesPlayed),
        //                    movesToString(generatedMoves)
        //                ), indent
        //            )
        //        }
        var hasValidMove = false
        for (move in generatedMoves) {
            if (ENGINE_DEBUG) {
                logDebug(
                    "Move: %s, score=%d. PV: [%d] %s".formatted(
                        UnapplyableMoveUtils.toShortString(move),
                        score,
                        alpha,
                        pv
                    ), indent
                )
            }
            // Do a recursive search
            val dumpBeforeApply = if (ENGINE_DEBUG) {
                ACEBoardUtils.stringDump(thinkingEngineBoard)
            } else {
                ""
            }
            ENGINE_DEBUG && breakpoint()
            thinkingEngineBoard.apply(move)
            ENGINE_DEBUG && breakpoint()

            var newHeight: Int? = null
            if (pvHeight != null && move == generatedMoves.first()) {
                newHeight = pvHeight + 1
            }
            val newMovesPlayed = if (ENGINE_DEBUG) {
                movesPlayed + move
            } else {
                listOf()
            }

            ENGINE_DEBUG && breakpoint()
            val recursiveVal = profile(StatsType.ALPHA_BETA) {
                -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, height + 1, newMovesPlayed, ply + 1)
            }
            if (!isLost(recursiveVal)) {
                hasValidMove = true
            }

            ENGINE_DEBUG && breakpoint()
            thinkingEngineBoard.unapply(
                move,
                white_king_or_rook_queen_side_moved,
                white_king_or_rook_king_side_moved,
                black_king_or_rook_queen_side_moved,
                black_king_or_rook_king_side_moved,
                fiftyMove
            )
            ENGINE_DEBUG && breakpoint()
            if (ENGINE_DEBUG) {
                val dumpAfterUnapply = ACEBoardUtils.stringDump(thinkingEngineBoard)
                if (dumpBeforeApply != dumpAfterUnapply) {
                    throw RuntimeException("Uh oh!")
                }
            }

            score = max(score, recursiveVal)
            if (score > alpha) {
                updatePv(pline, line, move)
                if (cutoffEnabled && score >= beta) {
                    if (LOAD_TEST) {
                        cutoffs++
                    }
                    updateKiller(0, move)
                    updateHistory(move, depth)
                    transpositionTable[depth, score, TranspositionTable.hashfBETA, move] = zobristHash
                    if (ENGINE_DEBUG) {
                        logDebug("BETA CUT OFF. $score >= $beta", indent)
                    }
                    return score
                }
                bestMove = move
                alpha = score
                hashf = TranspositionTable.hashfEXACT
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

    private fun orderMoves(moves: MutableList<Int>, depth: Int, ttMove: Int?, pvMove: Int?): Array<IntArray> {
        val killers = killerMoves[depth]

        val buckets= Array(5) { IntArray(moves.size) }
        val counts = IntArray(5)

        moves.forEach { move ->
            val bucket = when {
                move == pvMove -> 0
                UnapplyableMove.takePiece(move) != ColoredPieceType.NO_PIECE -> 1
                move == ttMove -> 2
                move == killers[0] || move == killers[1] -> 3
                else -> 4
            }
//            val bucket = 0
            val c = counts[bucket]++
            buckets[bucket][c] = move
        }
        return buckets
    }

    private fun quiesceSearch(alpha: Int, beta: Int, depth: Int, height: Int, movesPlayed: List<Int>): Int {
        var alpha = alpha
        val indent = if(ENGINE_DEBUG) {
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
            if(ENGINE_DEBUG) {
                logDebug("Beta cutoff", indent)
            }
            return score
        }
        if (score > alpha) {
            if(ENGINE_DEBUG) {
                logDebug("Alpha cutoff", indent)
            }
            alpha = score
        }

        val takeMoves: List<Int>
        try {
            takeMoves = thinkingEngineBoard.generateTakeMoves()
        } catch (e: KingEatingException) {
            if(ENGINE_DEBUG) {
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
                    if(ENGINE_DEBUG) {
                        logDebug("Beta cut-off", indent)
                    }
                    return beta
                }

                alpha = score
            }
            if (ENGINE_DEBUG) {
                logDebug(
                    "Move: %s, score=%d. PV: [%d] %s".formatted(
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

        if(ENGINE_DEBUG) {
            info("Applying after emitting: " + UnapplyableMoveUtils.toString(unapplyableMove))
        }
        currentEngineBoard.apply(unapplyableMove)

        // Start thinking about the next move
        // Emit the move that we found
        if(ENGINE_DEBUG) {
            info("Emitting move $bestMove")
        }
        emitNewMove(bestMove)
    }

    private fun err(s: String) {
        log.error(this.name + " > " + s)
    }

    private fun emitNewMove(move: Move?) {
        if(ENGINE_DEBUG) {
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
            if(ENGINE_DEBUG) {
                err("New incoming engineboard: $newIncomingEngineBoard")
            }
            throw NewEngineBoardException()
        } else if (newIncomingState.isPresent) {
            if(ENGINE_DEBUG) {
                err("Opponent move came in: $newIncomingState")
            }
            throw OpponentMoveCameInException()
        } else {
            val moveAtHeight = pv!!.getMoveAtHeight(0)
            if (moveAtHeight != null && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime) {
                if(ENGINE_DEBUG) {
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
                FieldUtils.coordinates(fromIdx(unapplyableMove).toInt()),
                FieldUtils.coordinates(targetIdx(unapplyableMove).toInt()),
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

        private enum class StatsType {
            MOVE_GEN,
            ORDER_MOVES,
            APPLY,
            UNAPPLY,
            QUIESCE,
            ALPHA_BETA
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


    fun updateKiller(depth: Int, move: Int) {
        if (killerMoves[depth][0] != move) {
            killerMoves[depth][1] = killerMoves[depth][0]
            killerMoves[depth][0] = move
        }
    }

    fun updateHistory(move: Int, depth: Int) {
        historyHeuristic[fromIdx(move).toInt()][targetIdx(move).toInt()] += depth * depth
    }

    private inline fun <T> profile(section: StatsType, block: () -> T): T {
        val start = System.nanoTime()
        val result = block()
        stats[section.ordinal] += (System.nanoTime() - start)
        return result
    }
}
