package nl.arthurvlug.chess.engine.ace.evaluation;

import java.util.HashMap;
import java.util.Map;

import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class ACEConstants {

	public static Map<PieceType, Integer> pieceValues() {
		Map<PieceType, Integer> map = new HashMap<>();
		map.put(PieceType.KING, Integer.MAX_VALUE / 2);
		map.put(PieceType.QUEEN, 975);
		map.put(PieceType.ROOK, 500);
		map.put(PieceType.BISHOP, 290);
		map.put(PieceType.KNIGHT, 280);
		map.put(PieceType.PAWN, 100);
		return map;
	}

	public static int pieceValue(PieceType pieceType) {
		return pieceValues().get(pieceType);
	}
}
