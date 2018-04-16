package nl.arthurvlug.chess.engine.ace;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_KING_BYTE;

public class PieceUtils {
	public static boolean isWhitePiece(final short coloredPiece) {
		return coloredPiece <= WHITE_KING_BYTE;
	}

	public static byte pieceColor(final ColoredPieceType coloredPiece) {
		throw new RuntimeException("");
	}

}
