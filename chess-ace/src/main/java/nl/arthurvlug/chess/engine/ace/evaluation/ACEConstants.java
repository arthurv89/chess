package nl.arthurvlug.chess.engine.ace.evaluation;

import java.util.HashMap;
import java.util.Map;

import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class ACEConstants {

	protected static int[] pawnPositionBonus = new int[] {
		 0,   0,   0,   0,   0,   0,   0,   0,
		 5,  10,  10, -25, -25,  10,  10,   5,
		-5,   0,  10,   5,   5, -10,   0,  -5,
		 0,  -5,   5,  25,  25,   5,  -5,   0,
		 5,  10,  20,  30,  30,  20,  10,   5,
		30,  40,  40,  42,  42,  40,  40,  30,
		50,  50,  50,  50,  50,  50,  50,  50,
		 0,   0,   0,   0,   0,   0,   0,   0
	};

	public static Map<PieceType, Integer> pieceValues() {
		Map<PieceType, Integer> map = new HashMap<>();
		map.put(PieceType.KING, 10000000);
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
