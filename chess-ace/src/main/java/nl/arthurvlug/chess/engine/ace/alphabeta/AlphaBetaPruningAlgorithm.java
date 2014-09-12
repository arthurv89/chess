package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.ArrayList;
import java.util.List;

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

	private static final int WHITE_WINS = Integer.MAX_VALUE-10;
	private static final int BLACK_WINS = -WHITE_WINS;
	private int nodesSearched = 0;

	private BoardEvaluator evaluator;

	public AlphaBetaPruningAlgorithm(BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public AceMove think(ACEBoard engineBoard) {
		nodesSearched = 0;

		int depth = 3;
		AceMove move = alphaBetaRoot(engineBoard, depth);
		return move;
	}

	private AceMove alphaBetaRoot(final ACEBoard engineBoard, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;

		
		engineBoard.generateSuccessorBoards();
		evaluateBoards(engineBoard.getSuccessorBoards());

//		successorBoards.sort(scoreComparator);
//
//		for (ACEBoard successorBoard : successorBoards) {
//			int evaluationScore = -alphaBeta(successorBoard, 1, -beta, -alpha);
//			if(evaluationScore >= 2000) {
//				return successorBoard.lastMove;
//			}
//		}

		List<ACEBoard> successorBoards = engineBoard.getSuccessorBoards();
		// TODO: Remove Sort?
		successorBoards.sort(scoreComparator);
		// TODO: Remove
		Preconditions.checkState(successorBoards.size() > 0);

		ACEBoard bestEngineBoard = new ACEBoard(EngineConstants.WHITE);
		ImmutableList<AceMove> newBestDepthMoves = null;
		for (ACEBoard successorBoard : successorBoards) {
			ImmutableList<AceMove> depthMoves = ImmutableList.<AceMove> of(successorBoard.lastMove);
			int score = -alphaBeta(successorBoard, depthMoves, depth-1, -beta, -alpha);
			successorBoard.setEvaluation(score);
			if (score > alpha) {
				alpha = score;
				newBestDepthMoves = depthMoves;
				bestEngineBoard = new ACEBoard(successorBoard);
			}
		}
		Preconditions.checkState(bestEngineBoard.getEvaluation() > Integer.MIN_VALUE);
		// TODO: Remove
		if(bestEngineBoard.lastMove == null) {
			System.out.println(newBestDepthMoves);
			System.out.println(bestEngineBoard);
			System.out.println();
		}
		return bestEngineBoard.lastMove;
	}

	private void evaluateBoards(List<ACEBoard> successorBoards) {
		for(ACEBoard successorBoard : successorBoards) {
			Integer score = successorBoard.sideDependentScore(evaluator);
			successorBoard.setEvaluation(score);
		}
	}

	private int alphaBeta(ACEBoard engineBoard, List<AceMove> depthMoves, int depth, int alpha, int beta) {
		nodesSearched++;
		log.debug(nodesSearched + " nodes searched (depth=" + depth + ")");

		if (engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}

		if (depth == 0) {
			return engineBoard.sideDependentScore(evaluator);
		}
		
		// TODO: Remove this
		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
			return engineBoard.sideDependentScore(evaluator);
		}

		List<ACEBoard> successorBoards = evaluateMoves(engineBoard);
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

		// TODO: Sort
		successorBoards.sort(scoreComparator);
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

			int newDepth = depth == 1 && successorBoard.lastMoveWasTakeMove
					? depth
					: depth-1;
			if(newDepth == depth) {
				log.info("Depth++ " + depthMoves.toString() + successorBoard.lastMove + "\n" + engineBoard.toString());
			}
			ImmutableList<AceMove> newDepthMoves = ImmutableList.<AceMove> builder()
					.addAll(depthMoves)
					.add(successorBoard.lastMove)
					.build();
			int value = -alphaBeta(successorBoard, newDepthMoves, newDepth, -beta, -alpha);

			if (value >= beta) {
				// Beta cut-off
				return beta;
			} else if (value > alpha) {
				alpha = value;
				bestDepthMoves = newDepthMoves;
			}
		}
		return alpha;
	}

	private List<ACEBoard> evaluateMoves(ACEBoard engineBoard) {
		// We are going to store our result boards here
		List<ACEBoard> scoredMoves = new ArrayList<>();

		List<AceMove> moves = MoveGenerator.generateMoves(engineBoard);
		for(AceMove move : moves) {
			ACEBoard copyEngineBoard = new ACEBoard(engineBoard);
			copyEngineBoard.apply(move);
			
			// TODO: Implement Checkmate
			Integer score = engineBoard.sideDependentScore(evaluator);
			copyEngineBoard.setEvaluation(score);
			
			scoredMoves.add(copyEngineBoard);
		}
		return scoredMoves;
	}
}
