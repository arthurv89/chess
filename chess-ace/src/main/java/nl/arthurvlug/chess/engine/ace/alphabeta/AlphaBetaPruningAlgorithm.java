package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.ArrayList;
import java.util.List;

import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.ScoreComparator;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;

public class AlphaBetaPruningAlgorithm {
	private static final ScoreComparator scoreComparator = new ScoreComparator();

	private static final int WHITE_WINS = Integer.MAX_VALUE;
	private static final int BLACK_WINS = -Integer.MAX_VALUE;
	private int nodesSearched = 0;

	private BoardEvaluator evaluator;

	public AlphaBetaPruningAlgorithm(BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public AceMove think(ACEBoard engineBoard) {
		nodesSearched = 0;

		int depth = 2;
		AceMove move = alphaBetaRoot(engineBoard, depth);
		return move;
	}

	private AceMove alphaBetaRoot(final ACEBoard engineBoard, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;

		
		List<ACEBoard> successorBoards = generateSuccessorBoards(engineBoard);

//		successorBoards.sort(scoreComparator);
//
//		for (ACEBoard successorBoard : successorBoards) {
//			int evaluationScore = -alphaBeta(successorBoard, 1, -beta, -alpha);
//			if(evaluationScore >= 2000) {
//				return successorBoard.lastMove;
//			}
//		}

		// TODO: Remove Sort?
		successorBoards.sort(scoreComparator);

		ACEBoard bestEngineBoard = new ACEBoard(Integer.MIN_VALUE);
		for (ACEBoard successorBoard : successorBoards) {
			int score = -alphaBeta(successorBoard, depth-1, -beta, -alpha);
			successorBoard.setEvaluation(score);
			if (score > alpha) {
				alpha = score;
				bestEngineBoard = new ACEBoard(successorBoard);
			}
		}
		return bestEngineBoard.lastMove;
	}

	private List<ACEBoard> generateSuccessorBoards(final ACEBoard board) {
		List<ACEBoard> successorBoards = new ArrayList<>();

		List<AceMove> moves = MoveGenerator.generateMoves(board);
		for (AceMove move : moves) {
			ACEBoard successorBoard = new ACEBoard(board);
			successorBoard.apply(move);

			// TODO: Decide on isCheck method. We could also remove it.
			// if(EngineUtils.isWhite(succColor) && whiteCheck(board,
			// copyEngineBoardMoves)) {
			// continue;
			// }
			// if(EngineUtils.isBlack(succColor) && blackCheck(board,
			// copyEngineBoardMoves)) {
			// continue;
			// }

			Integer score = sideDependentScore(successorBoard);
			successorBoard.setEvaluation(score);

			successorBoards.add(successorBoard);
		}
		return successorBoards;
	}

	private int alphaBeta(ACEBoard engineBoard, int depth, int alpha, int beta) {
		nodesSearched++;
//		log.debug(nodesSearched + " nodes searched");

		if (engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}

		if (depth == 0) {
			return sideDependentScore(engineBoard);
		}
		
		if(engineBoard.white_kings == 0 || engineBoard.black_kings == 0) {
			return sideDependentScore(engineBoard);
		}

		List<ACEBoard> succBoards = evaluateMoves(engineBoard);
		// if (engineBoard.whiteCheck() || engineBoard.blackCheck() ||
		// successorBoards.size > 0) {
		// if (isMate(succColor, engineBoard, engineBoard.whiteCheck() ||
		// engineBoard.blackCheck(), engineBoard.stalemate())) {
		// if (engineBoard.blackMate()) {
		// if (succColor == EngineConstants.BLACK) {
		// return BLACK_WINS - depth;
		// } else {
		// return WHITE_WINS + depth;
		// }
		// }
		//
		// if (engineBoard.whiteMate()) {
		// if (succColor == EngineConstants.BLACK) {
		// return WHITE_WINS + depth;
		// } else {
		// return BLACK_WINS - depth;
		// }
		// }
		//
		// return 0;
		// }
		// }

		// TODO: Sort
		succBoards.sort(scoreComparator);
		for (ACEBoard succBoard : succBoards) {
			ACEBoard copyEngineBoard = new ACEBoard(succBoard);
			copyEngineBoard.finalizeBitboards();
			
			// TODO: Implement this?
			// if (engineBoard.blackCheck() && succColor.isBlack()) {
			// continue; // Invalid move
			// }
			//
			// if (engineBoard.whiteCheck() && succColor.isWhite()) {
			// continue; // Invalid move
			// }

			int newDepth = depth == 1 && copyEngineBoard.lastMoveWasTakeMove
					? depth
					: depth-1;
			int value = -alphaBeta(copyEngineBoard, newDepth, -beta, -alpha);

			if (value >= beta) {
				// Beta cut-off
				return beta;
			} else if (value > alpha) {
				alpha = value;
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
			Integer score = sideDependentScore(copyEngineBoard);
			copyEngineBoard.setEvaluation(score);
			
			scoredMoves.add(copyEngineBoard);
		}
		return scoredMoves;

//		for (byte x = 0; x < 64; x++) {
//			PieceType piece = board.Squares[x].Piece;
//
//			// Make sure there is a piece on the square
//			if (piece == null)
//				continue;
//
//			// Make sure the color is the same color as the one we are moving.
//			if (piece.PieceColor != board.toMove)
//				continue;
//
//			// For each valid move for this piece
//			for (byte dst : piece.ValidMoves) {
//				ScoredMove scoredMove = new ScoredMove();
//
//				scoredMove.SrcEnginePosition = x;
//				scoredMove.DstEnginePosition = dst;
//
//				Piece pieceAttacked = board.Squares[scoredMove.DstEnginePosition].Piece;
//
//				// If the move is a capture add it's value to the score
//				if (pieceAttacked != null) {
//					scoredMove.Score += pieceAttacked.PieceValue;
//
//					if (piece.PieceValue < pieceAttacked.PieceValue) {
//						scoredMove.Score += pieceAttacked.PieceValue - piece.PieceValue;
//					}
//				}
//
//				if (!piece.Moved) {
//					scoredMove.Score += 10;
//				}
//
//				scoredMove.Score += piece.PieceActionValue;
//
//				// Add Score for Castling
//				if (!board.WhiteCastled && board.WhoseMove == EngineConstants.WHITE) {
//
//					if (piece.PieceType == PieceType.KING) {
//						if (scoredMove.DstEnginePosition != 62 && scoredMove.DstEnginePosition != 58) {
//							scoredMove.Score -= 40;
//						} else {
//							scoredMove.Score += 40;
//						}
//					}
//					if (piece.PieceType == PieceType.ROOK) {
//						scoredMove.Score -= 40;
//					}
//				}
//
//				if (!board.BlackCastled && board.WhoseMove == EngineConstants.BLACK) {
//					if (piece.PieceType == PieceType.KING) {
//						if (scoredMove.DstEnginePosition != 6 && scoredMove.DstEnginePosition != 2) {
//							scoredMove.Score -= 40;
//						} else {
//							scoredMove.Score += 40;
//						}
//					}
//					if (piece.PieceType == PieceType.ROOK) {
//						scoredMove.score -= 40;
//					}
//				}
//
//				scoredMoves.add(scoredMove);
//			}
//		}
	}

	private Integer sideDependentScore(ACEBoard engineBoard) {
		// TODO: Implement checkmate
		NormalScore score = (NormalScore) evaluator.evaluate(engineBoard);
		
		if (engineBoard.toMove == EngineConstants.BLACK) {
			return -score.getValue();
		} else {
			return score.getValue();
		}
	}
}
