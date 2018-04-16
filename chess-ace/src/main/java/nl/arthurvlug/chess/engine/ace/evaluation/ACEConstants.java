package nl.arthurvlug.chess.engine.ace.evaluation;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;

class ACEConstants {
	static int[] pawnPositionBonus = new int[] {
		 0,   0,   0,   0,   0,   0,   0,   0,
		50,  50,  50,  50,  50,  50,  50,  50,
		30,  40,  40,  42,  42,  40,  40,  30,
		 5,  10,  20,  30,  30,  20,  10,   5,
		 0,  -5,   5,  25,  25,   5,  -5,   0,
		-5,   0,  10,   5,   5, -10,   0,  -5,
		 5,  10,  10, -25, -25,  10,  10,   5,
		 0,   0,   0,   0,   0,   0,   0,   0
	};

	static int[] knightTable = new int[] {
		-50,-40,-30,-30,-30,-30,-40,-50,
		-40,-20,  0,  0,  0,  0,-20,-40,
		-30,  0, 10, 15, 15, 10,  0,-30,
		-30,  5, 15, 20, 20, 15,  5,-30,
		-30,  0, 15, 20, 20, 15,  0,-30,
		-30,  5, 10, 15, 15, 10,  5,-30,
		-40,-20,  0,  5,  5,  0,-20,-40,
		-50,-40,-20,-30,-30,-20,-40,-50,
	};
	
	static int[] bishopTable = new int[] {
		-20,-10,-10,-10,-10,-10,-10,-20,
		-10,  0,  0,  0,  0,  0,  0,-10,
		-10,  0,  5, 10, 10,  5,  0,-10,
		-10,  5,  5, 10, 10,  5,  5,-10,
		-10,  0, 10, 10, 10, 10,  0,-10,
		-10, 10, 10, 10, 10, 10, 10,-10,
		-10,  5,  0,  0,  0,  0,  5,-10,
		-20,-10,-40,-10,-10,-40,-10,-20,
	};

	static int[] kingTable = new int[] {
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-30, -40, -40, -50, -50, -40, -40, -30,
		-20, -30, -30, -40, -40, -30, -30, -20,
		-10, -20, -20, -20, -20, -20, -20, -10, 
		 20,  20,   0,   0,   0,   0,  20,  20,
		 20,  30,  10,   0,   0,  10,  30,  20
	};

	static int[] kingTableEndGame = new int[] {
		-50,-40,-30,-20,-20,-30,-40,-50,
		-30,-20,-10,  0,  0,-10,-20,-30,
		-30,-10, 20, 30, 30, 20,-10,-30,
		-30,-10, 30, 40, 40, 30,-10,-30,
		-30,-10, 30, 40, 40, 30,-10,-30,
		-30,-10, 20, 30, 30, 20,-10,-30,
		-30,-30,  0,  0,  0,  0,-30,-30,
		-50,-30,-30,-30,-30,-30,-30,-50
	};

	static int[] pieceValues = createPieceValuesArray();

	private static int[] createPieceValuesArray() {
		final int[] arr = new int[13];
		arr[WHITE_KING_BYTE] = 10000000;
		arr[WHITE_QUEEN_BYTE] = 975;
		arr[WHITE_ROOK_BYTE] = 500;
		arr[WHITE_BISHOP_BYTE] = 290;
		arr[WHITE_KNIGHT_BYTE] = 280;
		arr[WHITE_PAWN_BYTE] = 100;
		arr[BLACK_KING_BYTE] = 10000000;
		arr[BLACK_QUEEN_BYTE] = 975;
		arr[BLACK_ROOK_BYTE] = 500;
		arr[BLACK_BISHOP_BYTE] = 290;
		arr[BLACK_KNIGHT_BYTE] = 280;
		arr[BLACK_PAWN_BYTE] = 100;
		return arr;
	}
}
