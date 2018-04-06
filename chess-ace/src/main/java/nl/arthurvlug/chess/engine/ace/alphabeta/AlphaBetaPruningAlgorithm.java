package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.game.Move;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final int CURRENT_PLAYER_WINS = 1000000000;
	private static final int OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS;
	
	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;

	private final BoardEvaluator evaluator;
	private final Comparator<AbstractEngineBoard> scoreComparator;
//	private final TranspositionTable transpositionTable = new ;


	public AlphaBetaPruningAlgorithm(final BoardEvaluator evaluator,
									 final Comparator<AbstractEngineBoard> scoreComparator) {
		this.evaluator = evaluator;
		this.scoreComparator = scoreComparator;
	}

	public Move think(final AbstractEngineBoard engineBoard, final int depth) {
		Preconditions.checkArgument(depth > 0);
		
		nodesEvaluated = 0;

		return alphaBetaRoot(engineBoard, depth);
	}

	private Move alphaBetaRoot(final AbstractEngineBoard engineBoard, final int depth) {
		int bestScore = OTHER_PLAYER_WINS;

		List<Move> generatedMoves = engineBoard.generateMoves();
		final PriorityQueue<AbstractEngineBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard, generatedMoves);

		// TODO: Remove
		Preconditions.checkState(sortedSuccessorBoards.size() > 0);
		// Also check for mate in 1 moves

		Move bestMove = null;
		while(!sortedSuccessorBoards.isEmpty()) {
			final AbstractEngineBoard successorBoard = sortedSuccessorBoards.poll();

			final int score = -alphaBeta(successorBoard, OTHER_PLAYER_WINS, CURRENT_PLAYER_WINS, depth-1);
			if (score > bestScore) {
				bestScore = score;
				bestMove = successorBoard.getLastMove();
			}
		}
		
		return bestMove;
	}

	private int alphaBeta(final AbstractEngineBoard engineBoard, int alpha, final int beta, final int depth) {
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

		final PriorityQueue<AbstractEngineBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard, generatedMoves);
		while(!sortedSuccessorBoards.isEmpty()) {
			final AbstractEngineBoard successorBoard = sortedSuccessorBoards.poll();

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

	private int quiesceSearch(final AbstractEngineBoard engineBoard, int alpha, final int beta) {
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

		final PriorityQueue<AbstractEngineBoard> sortedSuccessorBoards = sortedSuccessorTakeBoards(engineBoard);
		while(!sortedSuccessorBoards.isEmpty()) {
			final AbstractEngineBoard successorBoard = sortedSuccessorBoards.poll();

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

	private <T extends AbstractEngineBoard> PriorityQueue<T> sortedSuccessorBoards(final T engineBoard, final List<Move> generatedMoves) {
		return priorityQueue(engineBoard.generateSuccessorBoards(generatedMoves));
	}

	private <T extends AbstractEngineBoard> PriorityQueue<T> sortedSuccessorTakeBoards(final T engineBoard) {
		return priorityQueue(engineBoard.generateSuccessorTakeBoards());
	}

	private <T extends AbstractEngineBoard> PriorityQueue<T> priorityQueue(final List<T> successorBoards) {
		evaluateBoards(successorBoards);
		
		final PriorityQueue<T> sortedSuccessorBoards = new PriorityQueue<>(scoreComparator);
		sortedSuccessorBoards.addAll(successorBoards);
		return sortedSuccessorBoards;
	}

	private void evaluateBoards(final List<? extends AbstractEngineBoard> boards) {
		boards.forEach(b -> evaluateBoard(b));
	}

	private Integer evaluateBoard(final AbstractEngineBoard board) {
		setSideDependentScore(board, evaluator);
		nodesEvaluated++;
		return board.getSideBasedEvaluation();
	}

	private static void setSideDependentScore(final AbstractEngineBoard board, final BoardEvaluator evaluator) {
		// TODO: Implement checkmate
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
