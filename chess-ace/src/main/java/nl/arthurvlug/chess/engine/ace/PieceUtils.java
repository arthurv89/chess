package nl.arthurvlug.chess.engine.ace;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_KING_BYTE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;

public class PieceUtils {
	public static boolean isWhitePiece(final short coloredPiece) {
		return coloredPiece <= WHITE_KING_BYTE;
	}

	public static byte pieceColor(final ColoredPieceType coloredPiece) {
		throw new RuntimeException("");
	}

	public static String type(final byte movingPiece) {
		switch (movingPiece) {
			case PAWN_BYTE: return "p";
			case KNIGHT_BYTE: return "N";
			case BISHOP_BYTE: return "B";
			case ROOK_BYTE: return "R";
			case QUEEN_BYTE: return "Q";
			case KING_BYTE: return "K";
			case NO_PIECE: return "";
		}
		throw new RuntimeException("Could not determine moving piece type");
	}
}
