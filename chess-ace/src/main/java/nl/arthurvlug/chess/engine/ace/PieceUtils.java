package nl.arthurvlug.chess.engine.ace;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;

public class PieceUtils {
	public static boolean isWhitePiece(final short coloredPiece) {
		return coloredPiece <= WHITE_KING_BYTE;
	}

	public static String type(final byte movingPiece) {
        return switch (movingPiece) {
            case WHITE_PAWN_BYTE, BLACK_PAWN_BYTE -> "p";
            case WHITE_KNIGHT_BYTE, BLACK_KNIGHT_BYTE -> "N";
            case WHITE_BISHOP_BYTE, BLACK_BISHOP_BYTE -> "B";
            case WHITE_ROOK_BYTE, BLACK_ROOK_BYTE -> "R";
            case WHITE_QUEEN_BYTE, BLACK_QUEEN_BYTE -> "Q";
            case WHITE_KING_BYTE, BLACK_KING_BYTE -> "K";
            case NO_PIECE -> "";
            default -> throw new RuntimeException("Could not determine moving piece type");
        };
    }

	public static char typeOrDot(final byte movingPiece) {
        return switch (movingPiece) {
            case WHITE_PAWN_BYTE, BLACK_PAWN_BYTE -> 'p';
            case WHITE_KNIGHT_BYTE, BLACK_KNIGHT_BYTE -> 'N';
            case WHITE_BISHOP_BYTE, BLACK_BISHOP_BYTE -> 'B';
            case WHITE_ROOK_BYTE, BLACK_ROOK_BYTE -> 'R';
            case WHITE_QUEEN_BYTE, BLACK_QUEEN_BYTE -> 'Q';
            case WHITE_KING_BYTE, BLACK_KING_BYTE -> 'K';
            case NO_PIECE -> '.';
            default -> throw new RuntimeException("Could not determine moving piece type");
        };
    }
}
