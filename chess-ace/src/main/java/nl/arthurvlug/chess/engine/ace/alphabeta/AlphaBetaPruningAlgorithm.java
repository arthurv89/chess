package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.ScoreComparator;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final ScoreComparator scoreComparator = new ScoreComparator();
	
	private static final int WHITE_WINS = 1000000000;
	private static final int BLACK_WINS = -WHITE_WINS;
	
	@Getter
	private int nodesEvaluated;

	private BoardEvaluator evaluator;

	public AlphaBetaPruningAlgorithm(BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public AceMove think(ACEBoard engineBoard, int depth) {
		nodesEvaluated = 0;

		AceMove move = alphaBetaRoot(engineBoard, depth);
		return move;
	}

	private AceMove alphaBetaRoot(final ACEBoard engineBoard, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;

		
		engineBoard.generateSuccessorBoards();

//		successorBoards.sort(scoreComparator);
//
//		for (ACEBoard successorBoard : successorBoards) {
//			int evaluationScore = -alphaBeta(successorBoard, 1, -beta, -alpha);
//			if(evaluationScore >= 2000) {
//				return successorBoard.lastMove;
//			}
//		}

		List<ACEBoard> successorBoards = engineBoard.getSuccessorBoards();
		// TODO: Remove
		Preconditions.checkState(successorBoards.size() > 0);

		ACEBoard bestEngineBoard = new ACEBoard(EngineConstants.WHITE);
		for (ACEBoard successorBoard : successorBoards) {
			ImmutableList<AceMove> depthMoves = ImmutableList.<AceMove> of(successorBoard.lastMove);

			int depthChange = depth == 1 && successorBoard.lastMoveWasTakeMove ? 0 : -1;
			int newDepth = depth + depthChange;
			
			int score = -alphaBeta(successorBoard, depthMoves, newDepth, -beta, -alpha, -newDepth);
			if (score > alpha) {
				alpha = score;
				bestEngineBoard = new ACEBoard(successorBoard);
			}
		}
		
		// TODO: Remove
		bestEngineBoard.finalizeBitboards();
		if(bestEngineBoard.getSideBasedEvaluation() == Integer.MIN_VALUE) {
			return successorBoards.get(0).lastMove;
		}
//		Preconditions.checkState(bestEngineBoard.getEvaluation() > Integer.MIN_VALUE);
		
		// TODO: Remove
		if(bestEngineBoard.lastMove == null) {
//			System.out.println(newBestDepthMoves);
//			System.out.println(bestEngineBoard);
//			System.out.println();
		}
		return bestEngineBoard.lastMove;
	}

	private int alphaBeta(ACEBoard engineBoard, List<AceMove> depthMoves, int depth, int alpha, int beta, int extraMoves) {
//		log.debug(nodesSearched + " nodes searched (depth=" + depth + ")");

		if (engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}

		if (depth == 0) {
			return evaluateBoard(engineBoard);
		}
		
		// TODO: Remove this
		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
//			engineBoard.sideDependentScore(evaluator);
			throw new RuntimeException("No kings :(");
		}

		List<ACEBoard> successorBoards = engineBoard.generateSuccessorBoards();
		
//		if (engineBoard.whiteCheck() || engineBoard.blackCheck() || successorBoards.size > 0) {
//			if (isMate(succColor, engineBoard, engineBoard.whiteCheck() || engineBoard.blackCheck(), engineBoard.stalemate())) {
//				if (engineBoard.blackMate()) {
//					if (succColor == EngineConstants.BLACK) {
//						return BLACK_WINS - depth;
//					} else {
//						return WHITE_WINS + depth;
//					}
//				}
//
//				if (engineBoard.whiteMate()) {
//					if (succColor == EngineConstants.BLACK) {
//						return WHITE_WINS + depth;
//					} else {
//						return BLACK_WINS - depth;
//					}
//				}
//
//				return 0;
//			}
//		}

		// TODO: Priority queue
		ImmutableList<AceMove> bestDepthMoves = null;
		for (ACEBoard successorBoard : successorBoards) {
			// TODO: Implement this?
			// if (engineBoard.blackCheck() && succColor.isBlack()) {
			// continue; // Invalid move
			// }
			//
			// if (engineBoard.whiteCheck() && succColor.isWhite()) {
			// continue; // Invalid move
			// }

			
			ImmutableList<AceMove> newDepthMoves = ImmutableList.<AceMove> builder()
					.addAll(depthMoves)
					.add(successorBoard.lastMove)
					.build();

			int newDepth;
			int newExtraMoves;
			if(depth == 1 && successorBoard.lastMoveWasTakeMove) {
				newDepth = depth;
				newExtraMoves = extraMoves +1;
				if(newExtraMoves > 100) {
					log.error("Error");
				}
			} else {
				newDepth = depth-1;
				newExtraMoves = extraMoves;
			}
			
			int value = -alphaBeta(successorBoard, newDepthMoves, newDepth, -beta, -alpha, newExtraMoves);

			if (value >= beta) {
//				log.debug("Beta cut-off");
				// Beta cut-off
				return beta;
			} else if (value > alpha) {
				alpha = value;
				bestDepthMoves = newDepthMoves;
			}
		}
		return alpha;
	}

	private Integer evaluateBoard(ACEBoard board) {
		nodesEvaluated++;
		return board.calculateSideDependentScore(evaluator);
	}

//	private List<ACEBoard> generateSuccessorBoards(ACEBoard engineBoard) {
//		// We are going to store our result boards here
//		List<ACEBoard> scoredMoves = new ArrayList<>();
//
//		List<AceMove> moves = MoveGenerator.generateMoves(engineBoard);
//		for(AceMove move : moves) {
//			ACEBoard copyEngineBoard = new ACEBoard(engineBoard);
//			copyEngineBoard.apply(move);
//			
//			// TODO: Implement Checkmate
//			nodesSearched++;
//			System.err.println("Skip calculating again!");
//			copyEngineBoard.calculateSideDependentScore(evaluator);
//			
//			scoredMoves.add(copyEngineBoard);
//		}
//		return scoredMoves;
//	}
}
