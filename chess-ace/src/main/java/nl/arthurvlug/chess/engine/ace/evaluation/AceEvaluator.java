package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import com.google.common.base.Function;

public class AceEvaluator implements BoardEvaluator {
	private Function<ACEBoard, Evaluation> scoreFunction = new Function<ACEBoard, Evaluation>() {
		public Evaluation apply(ACEBoard engineBoard) {
			int score = 0;
			score += ACEConstants.pieceValue(PieceType.KING) * Long.bitCount(engineBoard.white_kings);
			score += ACEConstants.pieceValue(PieceType.QUEEN) * Long.bitCount(engineBoard.white_queens);
			score += ACEConstants.pieceValue(PieceType.ROOK) * Long.bitCount(engineBoard.white_rooks);
			score += ACEConstants.pieceValue(PieceType.BISHOP) * Long.bitCount(engineBoard.white_bishops);
			score += ACEConstants.pieceValue(PieceType.KNIGHT) * Long.bitCount(engineBoard.white_knights);
			score += ACEConstants.pieceValue(PieceType.PAWN) * Long.bitCount(engineBoard.white_pawns);

			score -= ACEConstants.pieceValue(PieceType.KING) * Long.bitCount(engineBoard.black_kings);
			score -= ACEConstants.pieceValue(PieceType.QUEEN) * Long.bitCount(engineBoard.black_queens);
			score -= ACEConstants.pieceValue(PieceType.ROOK) * Long.bitCount(engineBoard.black_rooks);
			score -= ACEConstants.pieceValue(PieceType.BISHOP) * Long.bitCount(engineBoard.black_bishops);
			score -= ACEConstants.pieceValue(PieceType.KNIGHT) * Long.bitCount(engineBoard.black_knights);
			score -= ACEConstants.pieceValue(PieceType.PAWN) * Long.bitCount(engineBoard.black_pawns);
			
			return new NormalScore(score);
		}
	};

	@Override
	public NormalScore evaluate(AbstractEngineBoard board) {
		return (NormalScore) scoreFunction.apply((ACEBoard) board);
	}
}
