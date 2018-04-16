package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;

public class SimplePieceEvaluator extends BoardEvaluator {
	public Integer evaluate(ACEBoard engineBoard) {
		int score = 0;
		score += ACEConstants.pieceValues[WHITE_KING_BYTE] * Long.bitCount(engineBoard.white_kings);
		score += ACEConstants.pieceValues[WHITE_QUEEN_BYTE] * Long.bitCount(engineBoard.white_queens);
		score += ACEConstants.pieceValues[WHITE_ROOK_BYTE] * Long.bitCount(engineBoard.white_rooks);
		score += ACEConstants.pieceValues[WHITE_BISHOP_BYTE] * Long.bitCount(engineBoard.white_bishops);
		score += ACEConstants.pieceValues[WHITE_KNIGHT_BYTE] * Long.bitCount(engineBoard.white_knights);
		score += ACEConstants.pieceValues[WHITE_PAWN_BYTE] * Long.bitCount(engineBoard.white_pawns);

		score -= ACEConstants.pieceValues[BLACK_KING_BYTE] * Long.bitCount(engineBoard.black_kings);
		score -= ACEConstants.pieceValues[BLACK_QUEEN_BYTE] * Long.bitCount(engineBoard.black_queens);
		score -= ACEConstants.pieceValues[BLACK_ROOK_BYTE] * Long.bitCount(engineBoard.black_rooks);
		score -= ACEConstants.pieceValues[BLACK_BISHOP_BYTE] * Long.bitCount(engineBoard.black_bishops);
		score -= ACEConstants.pieceValues[BLACK_KNIGHT_BYTE] * Long.bitCount(engineBoard.black_knights);
		score -= ACEConstants.pieceValues[BLACK_PAWN_BYTE] * Long.bitCount(engineBoard.black_pawns);

		return score;
	}
}
