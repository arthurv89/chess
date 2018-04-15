package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;

import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;

public class SimplePieceEvaluator implements BoardEvaluator<ACEBoard, Integer> {
	@Override
	public Integer evaluate(ACEBoard engineBoard) {
		int score = 0;
		score += ACEConstants.pieceValues[KING.ordinal()] * Long.bitCount(engineBoard.white_kings);
		score += ACEConstants.pieceValues[QUEEN.ordinal()] * Long.bitCount(engineBoard.white_queens);
		score += ACEConstants.pieceValues[ROOK.ordinal()] * Long.bitCount(engineBoard.white_rooks);
		score += ACEConstants.pieceValues[BISHOP.ordinal()] * Long.bitCount(engineBoard.white_bishops);
		score += ACEConstants.pieceValues[KNIGHT.ordinal()] * Long.bitCount(engineBoard.white_knights);
		score += ACEConstants.pieceValues[PAWN.ordinal()] * Long.bitCount(engineBoard.white_pawns);

		score -= ACEConstants.pieceValues[KING.ordinal()] * Long.bitCount(engineBoard.black_kings);
		score -= ACEConstants.pieceValues[QUEEN.ordinal()] * Long.bitCount(engineBoard.black_queens);
		score -= ACEConstants.pieceValues[ROOK.ordinal()] * Long.bitCount(engineBoard.black_rooks);
		score -= ACEConstants.pieceValues[BISHOP.ordinal()] * Long.bitCount(engineBoard.black_bishops);
		score -= ACEConstants.pieceValues[KNIGHT.ordinal()] * Long.bitCount(engineBoard.black_knights);
		score -= ACEConstants.pieceValues[PAWN.ordinal()] * Long.bitCount(engineBoard.black_pawns);

		return score;
	}
}
