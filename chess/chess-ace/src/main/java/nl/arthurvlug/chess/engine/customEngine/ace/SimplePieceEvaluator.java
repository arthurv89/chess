package nl.arthurvlug.chess.engine.customEngine.ace;

import java.util.Map;

import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.EngineBoard;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;

import com.google.common.base.Function;

public class SimplePieceEvaluator implements BoardEvaluator {
	private final static Map<PieceType, Integer> pieceValues = ACEConstants.pieceValues();
	
	private Function<EngineBoard, Evaluation> scoreFunction = new Function<EngineBoard, Evaluation>() {
		public Evaluation apply(EngineBoard engineBoard) {
			int score = 0;
			score += ACEConstants.pieceValue(PieceType.QUEEN) * Long.bitCount(engineBoard.white_queens);
			score += ACEConstants.pieceValue(PieceType.ROOK) * Long.bitCount(engineBoard.white_rooks);
			score += ACEConstants.pieceValue(PieceType.BISHOP) * Long.bitCount(engineBoard.white_bishops);
			score += ACEConstants.pieceValue(PieceType.KNIGHT) * Long.bitCount(engineBoard.white_knights);
			score += ACEConstants.pieceValue(PieceType.PAWN) * Long.bitCount(engineBoard.white_pawns);
			
			score -= ACEConstants.pieceValue(PieceType.QUEEN) * Long.bitCount(engineBoard.black_queens);
			score -= ACEConstants.pieceValue(PieceType.ROOK) * Long.bitCount(engineBoard.black_rooks);
			score -= ACEConstants.pieceValue(PieceType.BISHOP) * Long.bitCount(engineBoard.black_bishops);
			score -= ACEConstants.pieceValue(PieceType.KNIGHT) * Long.bitCount(engineBoard.black_knights);
			score -= ACEConstants.pieceValue(PieceType.PAWN) * Long.bitCount(engineBoard.black_pawns);
			
			return new NormalScore(score);
		}
	};

	@Override
	public Evaluation evaluate(EngineBoard board) {
		return scoreFunction.apply(board);
	}

	private int pieceValue(PieceType pieceType) {
		return pieceValues.get(pieceType);
	}
}
