package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.ace.IncomingState;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.ThinkingTime;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.ace.transpositiontable.HashElement;
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.ThinkEvent;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.observers.Observers;

import static java.util.Collections.swap;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable.*;

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

	private BoardEvaluator evaluator;
	private final int quiesceMaxDepth;
	private int depth = Integer.MAX_VALUE;
	//	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
	private static final int HASH_TABLE_LENGTH = 1048576; // Must be a power or 2

	private static final TranspositionTable transpositionTable = new TranspositionTable(HASH_TABLE_LENGTH);
	private boolean quiesceEnabled = true;

	private ACEBoard thinkingEngineBoard;

	private volatile Optional<IncomingState> newIncomingState = Optional.empty();
	private volatile Optional<ACEBoard> newIncomingEngineBoard = Optional.empty();

	@Getter
	private final Observer<IncomingState> incomingMoves;
	private String name;
	private EventBus eventBus;
	private int iterator = 0;
	private Optional<Integer> priorityMove;
	private Subscriber<? super Move> subscriber;
	private int depthNow;

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
		this.incomingMoves = Observers.create(incomingState -> {
			final int timeLeft = thinkingEngineBoard.toMove == ColorUtils.WHITE
					? incomingState.getThinkingParams().getWhiteTime()
					: incomingState.getThinkingParams().getBlackTime();
			maxThinkingTime = thinkingTime(incomingState, timeLeft);
			info("Set max thinking time to " + maxThinkingTime);
			timer = Stopwatch.createStarted();
			newIncomingState = Optional.of(incomingState);
			iterator = 0;
		});
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


		while(true) {
			priorityMove = Optional.empty();
			try {
				thinkingEngineBoard = currentEngineBoard.cloneBoard();
				for (depthNow = 1; depthNow <= depth; depthNow++) {
					info(String.format("Start thinking for %d ms on depth %d. PriorityMove: %s",
							maxThinkingTime,
							depthNow,
							priorityMove.map(move -> UnapplyableMoveUtils.toString(move)).orElse("")));
					final Integer bestMove = alphaBetaRoot(depthNow, priorityMove);
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
					priorityMove = Optional.of(bestMove);
				}
			} catch (NewEngineBoardException e) {
				thinkingEngineBoard = newIncomingEngineBoard.get();
				newIncomingEngineBoard = Optional.empty();
			} catch (OpponentMoveCameInException e) {
				err("Our turn!");
				IncomingState incomingState = newIncomingState.get();
				final Move move = incomingState.getMove();
				if (move != null) {
					final String newMove = MoveUtils.toEngineMove(move);
					err("New move was: " + newMove);
					info("Applying " + newMove);
					currentEngineBoard.apply(newMove);
				}
				newIncomingState = Optional.empty();
				maxThinkingTime = thinkingTime(incomingState, incomingState.getThinkingParams().getWhiteTime()); // TODO: Also for BLACK
				timer = Stopwatch.createStarted();
			} catch (OutOfThinkingTimeException e) {
				// Out of time: just play the move
				final Integer move = priorityMove.get();
				applyAndEmitMove(move);
			}
		}
	}

	private void applyAndEmitMove(final Integer unapplyableMove) {
		final Optional<PieceType> promotionType = promotionType(unapplyableMove);
		final Move bestMove = new Move(
				FieldUtils.coordinates(UnapplyableMove.fromIdx(unapplyableMove)),
				FieldUtils.coordinates(UnapplyableMove.targetIdx(unapplyableMove)),
				promotionType);

		info("Applying after emitting: " + UnapplyableMoveUtils.toString(unapplyableMove));
		currentEngineBoard.apply(unapplyableMove);

		// Start thinking about the next move
		// Emit the move that we found
		info("Emitting move " + bestMove);
		emitNewMove(bestMove);
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

	private int thinkingTime(final IncomingState incomingState, final int timeLeft) {
		if(incomingState.getMove() == null) {
			return 1000; // First second should take at most 1 second
		}
		int moveNumber = currentEngineBoard.plyStack.size() / 2;
		if(moveNumber >= ThinkingTime.times.length) {
			return timeLeft / 20;
		}
		double grandmasterThinkingTime = ThinkingTime.times[moveNumber];
		return (int) (timeLeft * grandmasterThinkingTime);
//		final int timeDueToTimeLeft = (timeLeft - 2000 * thinkingEngineBoard.plyStack.size()) / 50;
//		final int timeDueToInitialTime = initialClockTime / 50;
//		return Math.min(timeDueToInitialTime, timeDueToTimeLeft);
	}

	private Integer alphaBetaRoot(final int depth, final Optional<Integer> priorityMove) {
		List<Integer> generatedMoves;
		try {
			generatedMoves = thinkingEngineBoard.generateMoves();
		} catch (KingEatingException e) {
			return null;
		}

//		final List<Integer> generatedMoves = Lists.newArrayList(priorityMove.get());
		reorder(generatedMoves, priorityMove);

		// TODO: Remove
		// TODO: Check for stalemate in the unit test
		Preconditions.checkState(generatedMoves.size() > 0);

		int alpha = OTHER_PLAYER_WINS/2;
		int beta = CURRENT_PLAYER_WINS;
		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;
		int score = alpha;
		for(int move : generatedMoves) {
//			info("Investigating " + UnapplyableMoveUtils.toString(move));
			final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
			thinkingEngineBoard.apply(move);
			// Do a recursive search
			final int val = -alphaBeta(-beta, -alpha, depth - 1);
			debugMoveStack(val);
			sysout("");
			thinkingEngineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);

			score = Math.max(val, score);
			if (score > alpha) {
				if (score >= beta) {
					cutoffs++;
					return move;
				}
				alpha = score;
				bestMove = move;
			}
		}
		info("[ABPruning Root] Best move score: " + alpha);
		return bestMove;
	}

	private void info(final String s) {
		log.info(this.name + " -> " + s);
	}

	private void performBrakeActions() {
		iterator++;

		if((iterator & 4095) != 0) {
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
		} else if(priorityMove.isPresent() && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime) {
			err("Out of time. Playing move " + UnapplyableMoveUtils.toString(priorityMove.get()) + " at depth " + (depthNow-1));
			throw new OutOfThinkingTimeException();
		}
	}

	private int alphaBeta(int alpha, final int beta, final int depth) {
		performBrakeActions();
		if (thinkingEngineBoard.getFiftyMoveClock() >= 50 || thinkingEngineBoard.getRepeatedMove() >= 3) {
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

		if (depth == 0) {
			// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
			return quiesceSearch(alpha, beta, quiesceMaxDepth);
		}

		List<Integer> generatedMoves;
		try {
			generatedMoves = thinkingEngineBoard.generateMoves();
		} catch (KingEatingException e) {
			return CURRENT_PLAYER_WINS;
		}

		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;

		int score = OTHER_PLAYER_WINS;
		for(final Integer move : generatedMoves) {
			// Do a recursive search
			final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
			thinkingEngineBoard.apply(move);
			int val = -alphaBeta(-beta, -alpha, depth-1);
			debugMoveStack(val);
			thinkingEngineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);

			score = Math.max(score, val);
			if (score > alpha) {
				if (score >= beta) {
					cutoffs++;
					transpositionTable.set(depth, score, hashfBETA, move, zobristHash);
					logDebug("BETA CUT OFF. " + score + " >= " + beta);
					return score;
				}
				bestMove = move;
				alpha = score;
				hashf = TranspositionTable.hashfEXACT;
			}
		}

		transpositionTable.set(depth, alpha, hashf, bestMove, zobristHash);
		return score;
	}

	private int losingScore() {
		return OTHER_PLAYER_WINS - thinkingEngineBoard.plyStack.size();
	}

	private int quiesceSearch(int alpha, final int beta, final int depth) {
		performBrakeActions();
		int score = calculateScore(thinkingEngineBoard);
		eventBus.post(new ThinkEvent(iterator));
		if(depth == 0 || !quiesceEnabled) {
			return score;
		}

		if (score >= beta) {
			return score;
		}
		if( score > alpha )
			alpha = score;

//		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
//
//

		final List<Integer> takeMoves;
		try {
			takeMoves = thinkingEngineBoard.generateTakeMoves();
		} catch (KingEatingException e) {
			return CURRENT_PLAYER_WINS;
		}
		boolean white_king_or_rook_queen_side_moved = thinkingEngineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = thinkingEngineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = thinkingEngineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = thinkingEngineBoard.black_king_or_rook_king_side_moved;
		for(Integer takeMove : takeMoves) {
			final int fiftyMove = thinkingEngineBoard.getFiftyMoveClock();
			thinkingEngineBoard.apply(takeMove);
			int val = -quiesceSearch(-beta, -alpha, depth-1);
			debugMoveStack(val);
			thinkingEngineBoard.unapply(takeMove,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);
//			debugMoveStack("Evaluating board\n{}Score: {}\n", thinkingEngineBoard.string(), val);

			score = Math.max(score, val);
			if (score > alpha) {
				if (val >= beta) {
					// Beta cut-off
					cutoffs++;
					logDebug("Beta cut-off");
					return beta;
				}

				alpha = score;
			}
		}
		return score;
	}

	private void logDebug(final String message) {
		if(MoveUtils.DEBUG) {
			log.info(message);
		}
	}

	private void sysout(final String message) {
		if(MoveUtils.DEBUG) {
			log.info(message);
		}
	}

	private void debugMoveStack(final int score) {
		if(MoveUtils.DEBUG) {
			final List<String> moveList = moveListStrings();
			System.out.printf("%s = %d%n", moveList, score);
		}
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

	private Optional<PieceType> promotionType(final int unapplyableMove) {
		byte promotionPiece = UnapplyableMove.promotionPiece(unapplyableMove);
		if(promotionPiece == NO_PIECE) {
			return Optional.empty();
		}
		return Optional.of(ColoredPieceType.from(promotionPiece).getPieceType());
	}

	private void reorder(final List<Integer> moves, final Optional<Integer> priorityMove) {
		priorityMove.flatMap(prioMove -> {
			Stream<Integer> range = IntStream.range(0, moves.size()).boxed();
			return range
					.flatMap((Integer i) -> findPrioPosition(moves, prioMove, i))
					.findFirst();
		})
				.ifPresent(pos -> swap(moves, 0, pos));
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
}
