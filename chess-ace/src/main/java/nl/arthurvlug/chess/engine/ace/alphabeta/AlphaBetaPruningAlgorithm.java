package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.PriorityQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.ScoreComparator;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

import com.google.common.base.Preconditions;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.board.Board;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

import static nl.arthurvlug.chess.utils.board.pieces.PieceType.KING;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final ScoreComparator scoreComparator = new ScoreComparator();
	
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

	public AceMove think(final ACEBoard engineBoard, final int depth) {
		Preconditions.checkArgument(depth > 0);
		
		nodesEvaluated = 0;

		return alphaBetaRoot(engineBoard, depth);
	}

	private AceMove alphaBetaRoot(final ACEBoard engineBoard, final int depth) {
		int bestScore = OTHER_PLAYER_WINS;

		List<AceMove> generatedMoves = MoveGenerator.generateMoves(engineBoard);
		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard, generatedMoves);

		// TODO: Remove
		Preconditions.checkState(sortedSuccessorBoards.size() > 0);
		// Also check for mate in 1 moves

		AceMove bestMove = null;
		while(!sortedSuccessorBoards.isEmpty()) {
			final ACEBoard successorBoard = sortedSuccessorBoards.poll();

			final int score = -alphaBeta(successorBoard, OTHER_PLAYER_WINS, CURRENT_PLAYER_WINS, depth-1);
			if (score > bestScore) {
				bestScore = score;
				bestMove = successorBoard.lastMove;
			}
		}
		
		return bestMove;
	}

	private int alphaBeta(final ACEBoard engineBoard, int alpha, final int beta, final int depth) {
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

		int bestScore = OTHER_PLAYER_WINS;

		List<AceMove> generatedMoves = MoveGenerator.generateMoves(engineBoard);
		for(AceMove move : generatedMoves) {
			final ColoredPiece takePiece = engineBoard.pieceAt(move.getToCoordinate());
			if(takePiece != null && takePiece.getPieceType() == KING) {
				return CURRENT_PLAYER_WINS;
			}
		}

		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorBoards(engineBoard, generatedMoves);
		while(!sortedSuccessorBoards.isEmpty()) {
			final ACEBoard successorBoard = sortedSuccessorBoards.poll();

			if (bestScore >= beta) {
				cutoffs++;
				break;
			}
			if (bestScore > alpha) {
				alpha = bestScore;
			}

//			final AceMove move = successorBoard.lastMove;
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

	private int quiesceSearch(final ACEBoard engineBoard, int _alpha, final int beta) {
		int stand_pat = engineBoard.getSideBasedEvaluation();
		return stand_pat;
//		if (stand_pat >= beta) {
//			return beta;
//		}
//		if( _alpha < stand_pat )
//			_alpha = stand_pat;
//
//		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
//		if(!engineBoard.lastMoveWasTakeMove) {
//			return stand_pat;
//		}
//
//
//
//		// TODO: Remove
//		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
//			throw new RuntimeException("No kings :(");
//		}
//
//		final PriorityQueue<ACEBoard> sortedSuccessorBoards = sortedSuccessorTakeBoards(engineBoard);
//		while(!sortedSuccessorBoards.isEmpty()) {
//			final ACEBoard successorBoard = sortedSuccessorBoards.poll();
//
//			final int value = -quiesceSearch(successorBoard, -beta, -newAlpha);
////			log.debug("Evaluating board\n{}Score: {}\n", successorBoard, value);
//
//			if (value >= beta) {
//				// Beta cut-off
//				cutoffs++;
////				log.debug("Beta cut-off");
//				return beta;
//			} else if (value > newAlpha) {
//				newAlpha = value;
//			}
//		}
//		return newAlpha;
	}

	private PriorityQueue<ACEBoard> sortedSuccessorBoards(final ACEBoard engineBoard, final List<AceMove> generatedMoves) {
		return priorityQueue(engineBoard.generateSuccessorBoards(generatedMoves));
	}

	private PriorityQueue<ACEBoard> sortedSuccessorTakeBoards(final ACEBoard engineBoard) {
		return priorityQueue(engineBoard.generateSuccessorTakeBoards());
	}

	private PriorityQueue<ACEBoard> priorityQueue(final List<ACEBoard> successorBoards) {
		evaluateBoards(successorBoards);
		
		final PriorityQueue<ACEBoard> sortedSuccessorBoards = new PriorityQueue<>(scoreComparator);
		sortedSuccessorBoards.addAll(successorBoards);
		return sortedSuccessorBoards;
	}

	private void evaluateBoards(final List<ACEBoard> boards) {
		boards.forEach(b -> evaluateBoard(b));
	}

	private Integer evaluateBoard(final ACEBoard board) {
		setSideDependentScore(board, evaluator);
		nodesEvaluated++;
		return board.getSideBasedEvaluation();
	}

	public static void setSideDependentScore(final ACEBoard board, final BoardEvaluator evaluator) {
		// TODO: Implement checkmate
		NormalScore score = (NormalScore) evaluator.evaluate(board);

		int sideDependentScore;
		if (board.toMove == EngineConstants.BLACK) {
			sideDependentScore = -score.getValue();
		} else {
			sideDependentScore = score.getValue();
		}
		board.setSideBasedEvaluation(sideDependentScore);
	}
}
