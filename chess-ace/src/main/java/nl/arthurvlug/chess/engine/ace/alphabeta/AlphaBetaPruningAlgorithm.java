package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.List;
import java.util.PriorityQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.ScoreComparator;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

import com.google.common.base.Preconditions;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final ScoreComparator scoreComparator = new ScoreComparator();
	
	private static final int WHITE_WINS = 1000000000;
	private static final int BLACK_WINS = -WHITE_WINS;
	
	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;

	private final BoardEvaluator evaluator;
//	private final TranspositionTable transpositionTable = new ;


	public AlphaBetaPruningAlgorithm(final BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public AceMove think(final ACEBoard engineBoard, final int depth) {
		Preconditions.checkArgument(depth > 0);
		
		nodesEvaluated = 0;

		return alphaBetaRoot(engineBoard, depth);
	}

	private AceMove alphaBetaRoot(final ACEBoard engineBoard, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;

		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard);
		final ACEBoard anyBoard = sortedSuccessorBoards.peek();
		// Also check for mate in 1 moves
		
		// TODO: Remove
		Preconditions.checkState(sortedSuccessorBoards.size() > 0);

		ACEBoard bestEngineBoard = null;
		while(!sortedSuccessorBoards.isEmpty()) {
			final ACEBoard successorBoard = sortedSuccessorBoards.poll();
			
			final int score = -alphaBeta(successorBoard, depth-1, -beta, -alpha);
			if (score > alpha) {
				alpha = score;
				bestEngineBoard = new ACEBoard(successorBoard);
				bestEngineBoard.setSideBasedEvaluation(score);
			}
		}
		
//		bestEngineBoard.finalizeBitboards();
		if(bestEngineBoard.getSideBasedEvaluation() == Integer.MIN_VALUE) {
			return anyBoard.lastMove;
		}
//		log.debug("{}", bestEngineBoard.lastMove);
//		log.debug("{}", bestEngineBoard.getSideBasedEvaluation());
		return bestEngineBoard.lastMove;
	}

	private int alphaBeta(final ACEBoard engineBoard, final int depth, int alpha, final int beta) {
		if (engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}

		if (depth == 0) {
			// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
			return quiesceSearch(engineBoard, alpha, beta);
		}
		
		// TODO: Remove
		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
			throw new RuntimeException("No kings :(");
		}

		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard);
		while(!sortedSuccessorBoards.isEmpty()) {
			final ACEBoard successorBoard = sortedSuccessorBoards.poll();
			
			final int value = -alphaBeta(successorBoard, depth-1, -beta, -alpha);
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

	private int quiesceSearch(final ACEBoard engineBoard, final int _alpha, final int beta) {
		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
		if(!engineBoard.lastMoveWasTakeMove) {
			return engineBoard.getSideBasedEvaluation();
		}
		
		if (engineBoard.getSideBasedEvaluation() >= beta) {
			return beta;
		}
		
		int newAlpha = _alpha;
		if (engineBoard.getSideBasedEvaluation() > newAlpha) {
			newAlpha = engineBoard.getSideBasedEvaluation();
		}


		
		// TODO: Remove
		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
			throw new RuntimeException("No kings :(");
		}

		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorTakeBoards(engineBoard);
		while(!sortedSuccessorBoards.isEmpty()) {
			final ACEBoard successorBoard = sortedSuccessorBoards.poll();
			
			final int value = -quiesceSearch(successorBoard, -beta, -newAlpha);
//			log.debug("Evaluating board\n{}Score: {}\n", successorBoard, value);

			if (value >= beta) {
				// Beta cut-off
				cutoffs++;
//				log.debug("Beta cut-off");
				return beta;
			} else if (value > newAlpha) {
				newAlpha = value;
			}
		}
		return newAlpha;
	}

	private PriorityQueue<ACEBoard> sortedSuccessorBoards(final ACEBoard engineBoard) {
		return priorityQueue(engineBoard.generateSuccessorBoards());
	}

	private PriorityQueue<ACEBoard> sortedSuccessorTakeBoards(final ACEBoard engineBoard) {
		return priorityQueue(engineBoard.generateSuccessorTakeBoards());
	}

	private PriorityQueue<ACEBoard> priorityQueue(final List<ACEBoard> successorBoards) {
		evaluateBoards(successorBoards);
		
		final PriorityQueue<ACEBoard> sortedSuccessorBoards = new PriorityQueue<ACEBoard>(scoreComparator);
		sortedSuccessorBoards.addAll(successorBoards);
		return sortedSuccessorBoards;
	}

	private void evaluateBoards(final List<ACEBoard> boards) {
		boards.forEach(b -> evaluateBoard(b));
	}

	private Integer evaluateBoard(final ACEBoard board) {
		nodesEvaluated++;
		return board.calculateSideDependentScore(evaluator);
	}
}
