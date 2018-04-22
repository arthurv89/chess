package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
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
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.ace.transpositiontable.HashElement;
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import static java.util.Collections.swap;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable.*;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final int CURRENT_PLAYER_WINS = 1000000000;
	private static final int OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS;

	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;
	@Getter
	private int hashHits;

	private BoardEvaluator evaluator;
	private final int quiesceMaxDepth;
	private int depth;
//	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
	private static final int HASH_TABLE_LENGTH = 1048576; // Must be a power or 2

	private static final TranspositionTable transpositionTable = new TranspositionTable(HASH_TABLE_LENGTH);
	private boolean quiesceEnabled = true;

	private ACEBoard engineBoard;

	public AlphaBetaPruningAlgorithm(final AceConfiguration configuration) {
		this.evaluator = configuration.getEvaluator();
		this.quiesceMaxDepth = configuration.getQuiesceMaxDepth();
		this.depth = configuration.getSearchDepth();
	}

	public Move think(final ACEBoard engineBoard, final ThinkingParams thinkingParams, final int initialClockTime) {
		Preconditions.checkArgument(depth > 0);
		this.engineBoard = engineBoard;

		cutoffs = 0;
		nodesEvaluated = 0;
		hashHits = 0;

//		Optional<Integer> priorityMove = Optional.of(UnapplyableMoveUtils.createMove("c7e6", engineBoard));
		Optional<Integer> priorityMove = Optional.empty();
		final int timeLeft = engineBoard.toMove == ColorUtils.WHITE
				? thinkingParams.getWhiteTime()
				: thinkingParams.getBlackTime();
		final int maxThinkingTime = thinkingTime(timeLeft, initialClockTime);
		final Stopwatch timer = Stopwatch.createStarted();

		final int thinkingDepth = thinkingDepth();
		for (int depthNow = 1; depthNow <= thinkingDepth; depthNow++) {
			try {
				logDebug("Start thinking on depth " + depthNow + ". PriorityMove: " + priorityMove.map(move -> UnapplyableMoveUtils.toString(move)).orElse(""));
				final Integer bestMove = alphaBetaRoot(depthNow, priorityMove, maxThinkingTime, timer);
				if (bestMove == null) {
					return null;
				}
				if(depthNow > 1000) {
					break;
				}
				priorityMove = Optional.of(bestMove);
			} catch (OutOfThinkingTimeException e) {
				// Out of time: just play the move
				System.err.println("Out of time. Playing depth " + (depthNow-1));
				break;
			}
		}
		final int unapplyableMove = priorityMove.get();
		final Optional<PieceType> promotionType = promotionType(unapplyableMove);
		return new Move(
				FieldUtils.coordinates(UnapplyableMove.fromIdx(unapplyableMove)),
				FieldUtils.coordinates(UnapplyableMove.targetIdx(unapplyableMove)),
				promotionType);
	}

	private int thinkingDepth() {
		if(engineBoard.moveStack.size() <= 1) {
			return 4;
		} else {
			return depth;
		}
	}

	private int thinkingTime(final int timeLeft, final int initialClockTime) {
		final int timeDueToTimeLeft = (timeLeft - 2000 * engineBoard.moveStack.size()) / 30;
		final int timeDueToTotal = initialClockTime / 50;
		return Math.min(timeDueToTotal, timeDueToTimeLeft);
	}

	private Integer alphaBetaRoot(final int depth, final Optional<Integer> priorityMove, final int maxThinkingTime, final Stopwatch timer) throws OutOfThinkingTimeException {
		List<Integer> generatedMoves;
		try {
			generatedMoves = engineBoard.generateMoves();
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
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
		int score = alpha;
		for(int move : generatedMoves) {
			if(shouldStopThinking(depth, maxThinkingTime, timer)) {
				// We should already have a move in iteration currentDepth-1.
				// Just throw an exception and make it return the previous iteration's move
				throw new OutOfThinkingTimeException();
			}
			// Do a recursive search
			final int fiftyMove = engineBoard.getFiftyMoveClock();
			engineBoard.apply(move);
//			int score = 0;
			final int val = -alphaBeta(-beta, -alpha, depth - 1, maxThinkingTime, timer);
			debugMoveStack(val);
			sysout("");
			engineBoard.unapply(move,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);

			score = Math.max(val, score);
			if (score > alpha) {
				if (score >= beta) {
					cutoffs++;
					logDebug("BETA cut off");
					System.out.println("Depth: " + depth + " Best move: " + UnapplyableMoveUtils.toString(move) + ". Score: " + score);
					return move;
				}
				alpha = score;
				bestMove = move;
			}
		}
		System.out.println("Depth: " + depth + " Best move: " + UnapplyableMoveUtils.toString(bestMove) + ". Score: " + alpha);
		return bestMove;
	}

	private boolean shouldStopThinking(final int depth, final int maxThinkingTime, final Stopwatch timer) {
		return depth > 1 && timer.elapsed(TimeUnit.MILLISECONDS) > maxThinkingTime;
	}

	private int alphaBeta(int alpha, final int beta, final int depth, final int maxThinkingTime, final Stopwatch timer) {
		if (engineBoard.getFiftyMoveClock() >= 50 || engineBoard.getRepeatedMove() >= 3) {
			return 0;
		}

		int hashf = hashfALPHA;
		int zobristHash = engineBoard.getZobristHash();
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
			return quiesceSearch(alpha, beta, quiesceMaxDepth, maxThinkingTime, timer);
		}

		List<Integer> generatedMoves;
		try {
			generatedMoves = engineBoard.generateMoves();
		} catch (KingEatingException e) {
			return CURRENT_PLAYER_WINS;
		}

		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;

		int score = OTHER_PLAYER_WINS;
		for(final Integer move : generatedMoves) {
			// Do a recursive search
			final int fiftyMove = engineBoard.getFiftyMoveClock();
			engineBoard.apply(move);
			int val = -alphaBeta(-beta, -alpha, depth-1, maxThinkingTime, timer);
			debugMoveStack(val);
			engineBoard.unapply(move,
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
		return OTHER_PLAYER_WINS - engineBoard.moveStack.size();
	}

	private int quiesceSearch(int alpha, final int beta, final int depth, final int maxThinkingTime, final Stopwatch timer) {
		if((nodesEvaluated^1023) == 0 && shouldStopThinking(depth, maxThinkingTime, timer)) {
			throw new OutOfThinkingTimeException();
		}
		int score = calculateScore(engineBoard);
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
			takeMoves = engineBoard.generateTakeMoves();
		} catch (KingEatingException e) {
			return CURRENT_PLAYER_WINS;
		}
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
		for(Integer takeMove : takeMoves) {
			final int fiftyMove = engineBoard.getFiftyMoveClock();
			engineBoard.apply(takeMove);
			int val = -quiesceSearch(-beta, -alpha, depth-1, maxThinkingTime, timer);
			debugMoveStack(val);
			engineBoard.unapply(takeMove,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved,
					fiftyMove);
//			debugMoveStack("Evaluating board\n{}Score: {}\n", engineBoard.string(), val);

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
			log.debug(message);
		}
	}

	private void sysout(final String message) {
		if(MoveUtils.DEBUG) {
			System.out.println(message);
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
		return engineBoard.moveStack.stream().map(m -> UnapplyableMoveUtils.toShortString(m)).collect(Collectors.toList());
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
}
