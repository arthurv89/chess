package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.ace.IncomingState;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.ace.transpositiontable.HashElement;
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.ThinkEvent;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.observers.Observers;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.alphabeta.PrincipalVariation.NO_MOVE;
import static nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable.*;
import static nl.arthurvlug.chess.utils.LogUtils.logDebug;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final int CURRENT_PLAYER_WINS = 1000000000;
	private static final int OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS;
	private ACEBoard currentEngineBoard;
	private int maxThinkingTime = Integer.MAX_VALUE;
	private Stopwatch timer = Stopwatch.createUnstarted();

	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;
	@Getter
	private int hashHits;

	BoardEvaluator evaluator;
	private final int quiesceMaxDepth;
	int depth = Integer.MAX_VALUE;
	//	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
	private static final int HASH_TABLE_LENGTH = 1048576; // Must be a power or 2

	private static final TranspositionTable transpositionTable = new TranspositionTable(HASH_TABLE_LENGTH);
	private boolean quiesceEnabled = true;

	ACEBoard thinkingEngineBoard;

	private volatile Optional<IncomingState> newIncomingState = Optional.empty();
	private volatile Optional<ACEBoard> newIncomingEngineBoard = Optional.empty();

	@Getter
	private final Observer<IncomingState> incomingMoves;
	private String name;
	private EventBus eventBus;
	private int iterator = 0;
	private Subscriber<? super Move> subscriber;
	private int depthNow;
	PrincipalVariation pv;
	boolean cutoffEnabled = true;

	public AlphaBetaPruningAlgorithm(final AceConfiguration configuration) {
		this(configuration, InitialACEBoard.createInitialACEBoard());
	}

	public AlphaBetaPruningAlgorithm(final AceConfiguration configuration, final ACEBoard initialACEBoard) {
		this.evaluator = configuration.getEvaluator();
		this.quiesceMaxDepth = configuration.getQuiesceMaxDepth();
		this.depth = configuration.getSearchDepth();

		Preconditions.checkArgument(depth > 0);

		this.currentEngineBoard = initialACEBoard;
		this.thinkingEngineBoard = currentEngineBoard.cloneBoard();
		this.incomingMoves = Observers.create(incomingState -> newIncomingState = Optional.of(incomingState));
	}

	public Observable<Move> startThinking(final ACEBoard engineBoard) {
		this.currentEngineBoard = engineBoard;
		return startThinking();
	}

	public Observable<Move> startThinking() {
		return Observable.create(sub -> {
			this.subscriber = sub;
			new Thread(() -> {
				try {
					think();
					sub.onCompleted();
				} catch (Exception e) {
					sub.onError(e);
				}
			}).start();
		});
	}

	private void think() {
		info("New thinking process");
		newIncomingState = Optional.empty();
		cutoffs = 0;
		nodesEvaluated = 0;
		hashHits = 0;


		pv = new PrincipalVariation();
		int startDepth = 0;
		try {
			thinkingEngineBoard = currentEngineBoard.cloneBoard();
			for (depthNow = startDepth; depthNow <= depth; depthNow++) {
				info(String.format("Start thinking for %d ms on depth %d. PV line: %s",
						maxThinkingTime,
						depthNow,
						Arrays.stream(pv.getLine()).boxed().filter(x -> x != NO_MOVE).map(m -> UnapplyableMoveUtils.toShortString(m)).collect(Collectors.toList())));
				final Integer bestMove = alphaBetaRoot(depthNow);
				if (bestMove == null) {
					err("No best move");
					emitNewMove(null);
					return;
				}
				if(depthNow == depth) {
					applyAndEmitMove(bestMove);
					return;
				}
				if (depthNow > 1000) {
					err("depth is extremely high");
					return;
				}
				info(String.format("Done thinking %d moves.", depthNow));
			}
			info(String.format("Done thinking ALL %d moves.", depth));
		} catch (NewEngineBoardException e) {
			// TODO: Where is newIncomingEngineBoard set?
			thinkingEngineBoard = newIncomingEngineBoard.get();
			newIncomingEngineBoard = Optional.empty();
			pv = new PrincipalVariation();
		} catch (OpponentMoveCameInException e) {
			err("Our turn!");
			IncomingState incomingState = newIncomingState.get();
			newIncomingState = Optional.empty();

			final Move move = incomingState.getMove();
			if (move != null) {
				final String newMove = MoveUtils.toEngineMove(move);
				err("New move was: " + newMove);

				if(pv.getPvHead() == UnapplyableMoveUtils.createMove(newMove, currentEngineBoard)) {
					System.arraycopy(pv.getRawLine(), 1, pv.getRawLine(), 0, pv.getRawLine().length-1);
					startDepth = depthNow-1;
				} else {
					pv = new PrincipalVariation();
					startDepth = 1;
				}

				info("Applying " + newMove);
				currentEngineBoard.apply(newMove);
			}

			maxThinkingTime = thinkingTime(incomingState.getThinkingParams());
			info("Set max thinking time to " + maxThinkingTime);
			timer = Stopwatch.createStarted();
			iterator = 0;
		} catch (OutOfThinkingTimeException e) {
			// Out of time: just play the move
			final int move = pv.getPvHead();
			System.arraycopy(pv.getRawLine(), 1, pv.getRawLine(), 0, pv.getRawLine().length-1);
			applyAndEmitMove(move);
		}
	}

	public Integer alphaBetaRoot(final int depth) {
		if (depth == 0) {
			return calculateScore(thinkingEngineBoard);
		}

		List<Integer> generatedMoves;
		try {
			generatedMoves = thinkingEngineBoard.generateMoves();
		} catch (KingEatingException e) {
			return null;
		}

//		final List<Integer> generatedMoves = Lists.newArrayList(priorityMove.get());
		int alpha = OTHER_PLAYER_WINS*2;
		int beta = CURRENT_PLAYER_WINS;
		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;

		final PrincipalVariation line = new PrincipalVariation();

		int score = Integer.MIN_VALUE;

		final Integer pvMove = pv.getMoveAtHeight(0);
		if (pvMove != NO_MOVE) {
			swapPvMove(generatedMoves, pvMove);
		}

		logDebug("Moves after []: %s".formatted(movesToString(generatedMoves)));
		for(int move : generatedMoves) {
			logDebug("Start Move: %s, score=%d. PV: [%d] %s".formatted(UnapplyableMoveUtils.toShortString(move), score, alpha, pv));
			Integer newHeight = null;
			if(move == generatedMoves.get(0)) {
				newHeight = 1;
			}

//			info("Investigating " + UnapplyableMoveUtils.toString(move));
			final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
			thinkingEngineBoard.apply(move);
			try {
				thinkingEngineBoard.generateMoves();
			} catch (KingEatingException e) {
				// The applied move is not valid. Ignore
				thinkingEngineBoard.unapply(move,
						white_king_or_rook_queen_side_moved,
						white_king_or_rook_king_side_moved,
						black_king_or_rook_queen_side_moved,
						black_king_or_rook_king_side_moved,
						fiftyMove);
				continue;
			}
			// Do a recursive search
			final int val = -alphaBeta(-beta, -alpha, depth - 1, line, newHeight, 1, ImmutableList.of(move));
//			debugMoveStack(val);
			thinkingEngineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);

			score = Math.max(val, score);
			if (score > alpha) {
				updatePv(pv, line, move);
				if (cutoffEnabled && score >= beta) {
					cutoffs++;
					logDebug(String.format("[ABPruning Root] Best move score: %d", score));
					return move;
 				}
				alpha = score;
				bestMove = move;
			}
			logDebug("End Move: %s, score=%d. PV: [%d] %s".formatted(UnapplyableMoveUtils.toShortString(move), score, alpha, pv));
		}
		info("[ABPruning Root] Best move score: " + alpha);
		return bestMove;
	}

	private int alphaBeta(int alpha,
						  final int beta,
						  final int depth,
						  final PrincipalVariation pline,
						  final Integer pvHeight,
						  final Integer height,
						  List<Integer> movesPlayed) {
		String indent = toIndent(movesPlayed);
//		logDebug("AlphaBeta. Depth=%s".formatted(depth), indent);
		performBrakeActions();
		if (thinkingEngineBoard.getFiftyMoveClock() >= 50 || thinkingEngineBoard.getRepeatedMove() >= 3) {
			logDebug("50 move / repeated move", indent);
			return 0;
		}

		int hashf = hashfALPHA;
		int zobristHash = thinkingEngineBoard.getZobristHash();
		final HashElement hashElement = transpositionTable.get(zobristHash);
		if (hashElement != null) {
			hashHits++;
			if (hashElement.depth >= depth) {
				if (hashElement.flags == hashfEXACT)
					return hashElement.val;
				if ((hashElement.flags == hashfALPHA) && (hashElement.val <= alpha))
					return alpha;
				if ((hashElement.flags == hashfBETA) && (hashElement.val >= beta))
					return beta;
			}
		}

		final PrincipalVariation line = new PrincipalVariation();
		if (depth == 0) {
			// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
			return quiesceSearch(alpha, beta, quiesceMaxDepth, height, movesPlayed);
		}

		List<Integer> generatedMoves;
		try {
			generatedMoves = thinkingEngineBoard.generateMoves();
		} catch (KingEatingException e) {
			logDebug("Stopping because the player can now take the king", indent);
			return CURRENT_PLAYER_WINS-height;
		}

		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;

		int score = alpha;
		if(pvHeight != null) {
			final Integer pvMove = pline.getMoveAtHeight(pvHeight);
			if (pvMove != NO_MOVE) {
				swapPvMove(generatedMoves, pvMove);
			}
		}

		logDebug("Moves after []: %s - %s".formatted(movesToString(movesPlayed), movesToString(generatedMoves)), indent);
		boolean hasValidMove = false;
		final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
		for(final int move : generatedMoves) {
			logDebug("Start Move: %s, score=%d. PV: [%d] %s".formatted(UnapplyableMoveUtils.toShortString(move), score, alpha, pv), indent);
			// Do a recursive search
			thinkingEngineBoard.apply(move);

			Integer newHeight = null;
			if(pvHeight != null && move == generatedMoves.get(0)) {
				newHeight = pvHeight + 1;
			}

			ImmutableList<Integer>  newMovesPlayed;
			if(MoveUtils.DEBUG) {
				newMovesPlayed = ImmutableList.<Integer>builder().addAll(movesPlayed).add(move).build();
			} else {
				newMovesPlayed = ImmutableList.of();
			}
			int val = -alphaBeta(-beta, -alpha, depth-1, line, newHeight, height+1, newMovesPlayed);
            if (!isLost(val)) {
                hasValidMove = true;
            }

//			debugMoveStack(val);
			thinkingEngineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);

			score = Math.max(score, val);
			if (score > alpha) {
				if (cutoffEnabled && score >= beta) {
					cutoffs++;
					transpositionTable.set(depth, score, hashfBETA, move, zobristHash);
					logDebug("BETA CUT OFF. " + score + " >= " + beta, indent);
					return score;
				}
				updatePv(pline, line, move);
				bestMove = move;
				alpha = score;
				hashf = TranspositionTable.hashfEXACT;
			}
			logDebug("End Move: %s, score=%d. PV: [%d] %s".formatted(UnapplyableMoveUtils.toShortString(move), score, alpha, pv), indent);
		}

		if(!hasValidMove) {
//			thinkingEngineBoard.toMove = opponent(thinkingEngineBoard.toMove);
//			thinkingEngineBoard.apply(bestMove);
//			boolean isCheckmate = thinkingEngineBoard.canTakeKing();
//			if(isCheckmate) {
//				return score;
//			}

			thinkingEngineBoard.toMove = opponent(thinkingEngineBoard.toMove);
			thinkingEngineBoard.mutateGeneralBoardOccupation();
//			thinkingEngineBoard.apply(bestMove);
			boolean opponentCanTakeKing = thinkingEngineBoard.canTakeKing();
//			thinkingEngineBoard.toMove = opponent(thinkingEngineBoard.toMove);
//			thinkingEngineBoard.unapply(bestMove,
//					thinkingEngineBoard.white_king_or_rook_queen_side_moved,
//					thinkingEngineBoard.white_king_or_rook_king_side_moved,
//					thinkingEngineBoard.black_king_or_rook_queen_side_moved,
//					thinkingEngineBoard.black_king_or_rook_king_side_moved,
//					thinkingEngineBoard.getFiftyMoveClock());

			if(!opponentCanTakeKing) {
				// Stalemate
				return 0;
			}
		}

		transpositionTable.set(depth, alpha, hashf, bestMove, zobristHash);
		return score;
	}

	private static boolean isLost(int val) {
		return val <= OTHER_PLAYER_WINS / 2;
	}

	private static String movesToString(List<Integer> moves) {
		return moves.stream().map(UnapplyableMoveUtils::toShortString).collect(Collectors.joining(", "));
	}

	private int quiesceSearch(int alpha, final int beta, final int depth, int height, List<Integer> movesPlayed) {
		String indent = toIndent(movesPlayed);
		performBrakeActions();
		int score = calculateScore(thinkingEngineBoard);
		eventBus.post(new ThinkEvent(iterator));
		if(depth == 0 || !quiesceEnabled) {
			return score;
		}

		if (score >= beta) {
			logDebug("Beta cutoff", indent);
			return score;
		}
		if( score > alpha ) {
			logDebug("Alpha cutoff", indent);
			alpha = score;
		}

		List<Integer> takeMoves;
		try {
			takeMoves = thinkingEngineBoard.generateTakeMoves();
		} catch (KingEatingException e) {
			logDebug("Eating king", indent);
			return CURRENT_PLAYER_WINS-height;
		}

		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;

		for(Integer move : takeMoves) {
			final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
			thinkingEngineBoard.apply(move);
			int val = -quiesceSearch(-beta, -alpha, depth-1, height+1, movesPlayed);
//			debugMoveStack(val);
			thinkingEngineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);
//			debugMoveStack("Evaluating board\n{}Score: {}\n", thinkingEngineBoard.string(), val);

			score = Math.max(score, val);
			if (score > alpha) {
				if (cutoffEnabled && val >= beta) {
					// Beta cut-off
					cutoffs++;
					logDebug("Beta cut-off", indent);
					return beta;
				}

				alpha = score;
			}
			logDebug("End Move: %s, score=%d. PV: [%d] %s".formatted(UnapplyableMoveUtils.toShortString(move), score, alpha, pv), indent);
		}
		return score;
	}

	private void updatePv(final PrincipalVariation pv, final PrincipalVariation line, final Integer move) {
		if(isWorsePv(pv, line, move)) {
			return;
		}
		pv.getRawLine()[0] = move;
		System.arraycopy(line.getRawLine(), 0, pv.getRawLine(), 1, line.getRawLine().length-1);
		pv.setLineElements(line.getLineElements() + 1);
	}

	private boolean isWorsePv(final PrincipalVariation pv, final PrincipalVariation line, final Integer move) {
		return pv.getRawLine()[0] == move
				&& Arrays.equals(
					Arrays.copyOfRange(pv.getLine(), 1, 1+line.getLineElements()),
					Arrays.copyOfRange(line.getLine(), 0, line.getLineElements()));
	}

	private void applyAndEmitMove(final Integer unapplyableMove) {
		final Move bestMove = toMove(unapplyableMove);

		info("Applying after emitting: " + UnapplyableMoveUtils.toString(unapplyableMove));
		currentEngineBoard.apply(unapplyableMove);

		// Start thinking about the next move
		// Emit the move that we found
		info("Emitting move " + bestMove);
		emitNewMove(bestMove);
	}

	public static Move toMove(Integer unapplyableMove) {
		final Optional<PieceType> promotionType = promotionType(unapplyableMove);
		final Move bestMove = new Move(
				FieldUtils.coordinates(UnapplyableMove.fromIdx(unapplyableMove)),
				FieldUtils.coordinates(UnapplyableMove.targetIdx(unapplyableMove)),
				promotionType);
		return bestMove;
	}

	private void err(final String s) {
		log.error(this.name + " > " + s);
	}

	private void emitNewMove(final Move move) {
		info("Emitting new move: " + move);
		this.maxThinkingTime = Integer.MAX_VALUE;
		iterator = 0;
		subscriber.onNext(move);
	}

	private int thinkingTime(final ThinkingParams thinkingParams) {
		final int timeLeft = currentEngineBoard.toMove == ColorUtils.WHITE
				? thinkingParams.getWhiteTime()
				: thinkingParams.getBlackTime();

		if(currentEngineBoard.plyStack.size() <= 1) {
			return 1000; // First second should take at most 1 second
		}
//		double grandmasterThinkingTime = ThinkingTime.times[moveNumber];
//		int time = (int) (timeLeft * grandmasterThinkingTime);
//		return time;
		final int timeDueToTimeLeft = (timeLeft - 2000 * thinkingEngineBoard.plyStack.size()) / 50;
//		final int timeDueToInitialTime = initialClockTime / 50;
//		return Math.min(timeDueToInitialTime, timeDueToTimeLeft);
		return timeDueToTimeLeft;
	}

	private void performBrakeActions() {
		iterator++;

		if((iterator & 0b111111111111) != 0) {
			return;
		}

//		info(String.format("Time spent %d/%d ms", timer.elapsed(TimeUnit.MILLISECONDS), maxThinkingTime));
		// TODO: Wrap the incoming events into an IncomingEvent object and check here whether is Optional.empty or not
		if(newIncomingEngineBoard.isPresent()) {
			err("New incoming engineboard: " + newIncomingEngineBoard);
			throw new NewEngineBoardException();
		} else if(newIncomingState.isPresent()) {
			err("Opponent move came in: " + newIncomingState);
			throw new OpponentMoveCameInException();
		} else {
			final Integer moveAtHeight = pv.getMoveAtHeight(0);
			if(moveAtHeight != null && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime) {
				err("Out of time. Playing move " + UnapplyableMoveUtils.toString(moveAtHeight) + " at depth " + (depthNow-1));
				throw new OutOfThinkingTimeException();
			}
		}
	}


	private int losingScore() {
		return OTHER_PLAYER_WINS - thinkingEngineBoard.plyStack.size();
	}

	private void info(final String s) {
		log.info(this.name + " -> " + s);
	}

	private boolean moveListContainsAll(String... moves) {
		ImmutableList<String> c = ImmutableList.copyOf(moves);
		return moveListStrings().containsAll(c);
	}

	private List<String> moveListStrings() {
		return thinkingEngineBoard.plyStack.stream().map(m -> UnapplyableMoveUtils.toShortString(m)).collect(Collectors.toList());
	}

	private int calculateScore(final ACEBoard board) {
		nodesEvaluated++;
		Integer score = evaluator.evaluate(board);

		if (board.getToMove() == ColorUtils.BLACK) {
			return -score;
		}
		return score;
	}

	private static Optional<PieceType> promotionType(final int unapplyableMove) {
		byte promotionPiece = UnapplyableMove.promotionPiece(unapplyableMove);
		if(promotionPiece == NO_PIECE) {
			return Optional.empty();
		}
		return Optional.of(ColoredPieceType.from(promotionPiece).getPieceType());
	}

	private void swapPvMove(final List<Integer> moves, final int priorityMove) {
		final int idx = moves.indexOf(priorityMove);
		Preconditions.checkArgument(idx != -1, "Could not find move " + UnapplyableMoveUtils.toShortString(priorityMove) + " (" + priorityMove + ") in move list!");
		Collections.swap(moves, 0, idx);
	}

	private Stream<Integer> findPrioPosition(final List<Integer> generatedMoves, final Integer prioMove, final Integer i) {
		if (generatedMoves.get(i).equals(prioMove)) {
			return Stream.of(i);
		}
		return Stream.empty();
	}

	private boolean shouldPause() {
		return moveListContainsAll("f1e1", "g7g6");
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	public void disableQuiesce() {
		this.quiesceEnabled = false;
	}

	public void useSimplePieceEvaluator() {
		this.evaluator = new SimplePieceEvaluator();
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setEventBus(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	private static String toIndent(List<Integer> generatedMoves) {
		return generatedMoves.stream().map(x -> "  ").collect(Collectors.joining());
	}
}
