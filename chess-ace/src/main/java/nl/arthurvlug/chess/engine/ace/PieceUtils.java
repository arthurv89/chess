package nl.arthurvlug.chess.engine.ace;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;

public class PieceUtils {
	public static boolean isWhitePiece(final short coloredPiece) {
		return coloredPiece <= WHITE_KING_BYTE;
	}

	public static byte pieceColor(final ColoredPieceType coloredPiece) {
		throw new RuntimeException("");
	}

	public static String type(final byte movingPiece) {
		switch (movingPiece) {
			case WHITE_PAWN_BYTE: case BLACK_PAWN_BYTE: return "p";
			case WHITE_KNIGHT_BYTE: case BLACK_KNIGHT_BYTE: return "N";
			case WHITE_BISHOP_BYTE: case BLACK_BISHOP_BYTE: return "B";
			case WHITE_ROOK_BYTE: case BLACK_ROOK_BYTE: return "R";
			case WHITE_QUEEN_BYTE: case BLACK_QUEEN_BYTE: return "Q";
			case WHITE_KING_BYTE: case BLACK_KING_BYTE: return "K";
			case NO_PIECE: return "";
		}
		throw new RuntimeException("Could not determine moving piece type");
	}
}
