package nl.arthurvlug.chess.engine.ace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

import static nl.arthurvlug.chess.utils.board.pieces.Color.BLACK;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;

@AllArgsConstructor
@Getter
public enum ColoredPieceType {
	;

	public static final byte NO_PIECE = 0;

	public static final byte WHITE_PAWN_BYTE   = 1;
	public static final byte WHITE_KNIGHT_BYTE = 2;
	public static final byte WHITE_BISHOP_BYTE = 3;
	public static final byte WHITE_ROOK_BYTE   = 4;
	public static final byte WHITE_QUEEN_BYTE  = 5;
	public static final byte WHITE_KING_BYTE   = 6;

	public static final byte BLACK_PAWN_BYTE   = 7;
	public static final byte BLACK_KNIGHT_BYTE = 8;
	public static final byte BLACK_BISHOP_BYTE = 9;
	public static final byte BLACK_ROOK_BYTE   = 10;
	public static final byte BLACK_QUEEN_BYTE  = 11;
	public static final byte BLACK_KING_BYTE   = 12;

	public static ColoredPiece from(final byte pieceOnField) {
		switch (pieceOnField) {
			case WHITE_PAWN_BYTE  : return new ColoredPiece(PAWN, WHITE);
			case WHITE_KNIGHT_BYTE: return new ColoredPiece(KNIGHT, WHITE);
			case WHITE_BISHOP_BYTE: return new ColoredPiece(BISHOP, WHITE);
			case WHITE_ROOK_BYTE  : return new ColoredPiece(ROOK, WHITE);
			case WHITE_QUEEN_BYTE : return new ColoredPiece(QUEEN, WHITE);
			case WHITE_KING_BYTE  : return new ColoredPiece(KING, WHITE);

			case BLACK_PAWN_BYTE  : return new ColoredPiece(PAWN, BLACK);
			case BLACK_KNIGHT_BYTE: return new ColoredPiece(KNIGHT, BLACK);
			case BLACK_BISHOP_BYTE: return new ColoredPiece(BISHOP, BLACK);
			case BLACK_ROOK_BYTE  : return new ColoredPiece(ROOK, BLACK);
			case BLACK_QUEEN_BYTE : return new ColoredPiece(QUEEN, BLACK);
			case BLACK_KING_BYTE  : return new ColoredPiece(KING, BLACK);
		}
		return null;
	}
}
