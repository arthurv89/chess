package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.game.Move;

import static java.util.Collections.swap;

@Slf4j
public class AlphaBetaPruningAlgorithm<T extends AbstractEngineBoard<T>> {
	private static final int CURRENT_PLAYER_WINS = 1000000000;
	private static final int OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS;
	
	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;

	private final BoardEvaluator evaluator;
//	private final TranspositionTable transpositionTable = new ;

	public AlphaBetaPruningAlgorithm(final BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public Move think(final T engineBoard, final int depth) {
		Preconditions.checkArgument(depth > 0);
		
		nodesEvaluated = 0;

		Optional<Move> priorityMove = Optional.empty();
		for (int depthNow = 0; depthNow < depth; depthNow++) {
			final Move bestMove = alphaBetaRoot(engineBoard, depth, priorityMove);
			if(bestMove == null) {
				return null;
			}
			priorityMove = Optional.of(bestMove);
		}
		return priorityMove.get();
	}

	private Move alphaBetaRoot(final T engineBoard, final int depth, final Optional<Move> priorityMove) {
		int bestScore = OTHER_PLAYER_WINS;

		final List<Move> generatedMoves = engineBoard.generateMoves();
		reorder(generatedMoves, priorityMove);
		final List<T> successorBoards = engineBoard.generateSuccessorBoards(generatedMoves);

		// TODO: Remove
		Preconditions.checkState(successorBoards.size() > 0);

		Move bestMove = null;
		for(T successorBoard : successorBoards) {
			final int score = -alphaBeta(successorBoard, OTHER_PLAYER_WINS, CURRENT_PLAYER_WINS, depth-1);
			if (score > bestScore) {
				bestScore = score;
				bestMove = successorBoard.getLastMove();
			}
		}
		
		return bestMove;
	}

	private void reorder(final List<Move> moves, final Optional<Move> priorityMove) {
		priorityMove.flatMap(prioMove -> {
			Stream<Integer> range = IntStream.range(0, moves.size()).boxed();
			return range
				.flatMap((Integer i) -> findPrioPosition(moves, prioMove, i))
				.findFirst();
		})
		.ifPresent(pos -> swap(moves, 0, pos));
	}

	private Stream<Integer> findPrioPosition(final List<Move> generatedMoves, final Move prioMove, final Integer i) {
		if (generatedMoves.get(i).equals(prioMove)) {
			return Stream.of(i);
		}
		return Stream.empty();
	}

	private int alphaBeta(final T engineBoard, int alpha, final int beta, final int depth) {
		if (engineBoard.getFiftyMove() >= 50 || engineBoard.getRepeatedMove() >= 3) {
			return 0;
		}

		if (depth == 0) {
			// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
			return quiesceSearch(engineBoard, alpha, beta);
		}
		
		// TODO: Remove
		if(engineBoard.hasNoKing()) {
			throw new RuntimeException("No kings :(");
		}

		int bestScore = OTHER_PLAYER_WINS;

		List<Move> generatedMoves = engineBoard.generateMoves();
		if (engineBoard.opponentIsInCheck(generatedMoves)) {
			return CURRENT_PLAYER_WINS;
		}

		final List<T> successorBoards = engineBoard.generateSuccessorBoards(generatedMoves);
		for(T successorBoard : successorBoards) {
			if (bestScore >= beta) {
				cutoffs++;
				break;
			}
			if (bestScore > alpha) {
				alpha = bestScore;
			}

//			final Move move = successorBoard.lastMove;
//			if (move.is_captured_piece_a_king())
//			{
//				return 900 + level; // Opponent's king can be captured. That means he is check-mated.
//			}

			// Do a recursive search
			int score = -alphaBeta(successorBoard, -beta, -alpha, depth-1);

			if (score > bestScore) {
				// Store the best value so far.
				bestScore = score;
			}
		}
		return bestScore;
	}

	private int quiesceSearch(final T engineBoard, int alpha, final int beta) {
		setSideDependentScore(engineBoard);
		int stand_pat = engineBoard.getSideBasedEvaluation();
		if (stand_pat >= beta) {
			return beta;
		}
		if( alpha < stand_pat )
			alpha = stand_pat;

//		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
//		if(!engineBoard.lastMoveWasTakeMove) {
//			return stand_pat;
//		}
//
//
//
		if(engineBoard.hasNoKing()) {
			return OTHER_PLAYER_WINS;
		}

		final List<T> successorBoards = engineBoard.generateSuccessorTakeBoards();
		for(T successorBoard : successorBoards) {
			final int value = -quiesceSearch(successorBoard, -beta, -alpha);
//			log.debug("Evaluating board\n{}Score: {}\n", successorBoard, value);

			if (value >= beta) {
				// Beta cut-off
				cutoffs++;
//				log.debug("Beta cut-off");
				return beta;
			} else if (value > alpha) {
				alpha = value;
			}
		}
		return alpha;
	}

	private void setSideDependentScore(final T board) {
		nodesEvaluated++;
		NormalScore score = (NormalScore) evaluator.evaluate(board);

		int sideDependentScore;
		if (board.getToMove() == EngineConstants.BLACK) {
			sideDependentScore = -score.getValue();
		} else {
			sideDependentScore = score.getValue();
		}
		board.setSideBasedEvaluation(sideDependentScore);
	}
}
