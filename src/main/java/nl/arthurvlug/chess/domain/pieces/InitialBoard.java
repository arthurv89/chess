package nl.arthurvlug.chess.domain.pieces;

import nl.arthurvlug.chess.domain.board.Board;

import com.atlassian.fugue.Option;

public class InitialBoard extends Board {
	public InitialBoard() {
		setField(0, 0, Option.some(new ColoredPiece(PieceType.ROOK, Color.WHITE)));
		setField(1, 0, Option.some(new ColoredPiece(PieceType.KNIGHT, Color.WHITE)));
		setField(2, 0, Option.some(new ColoredPiece(PieceType.BISHOP, Color.WHITE)));
		setField(3, 0, Option.some(new ColoredPiece(PieceType.QUEEN, Color.WHITE)));
		setField(4, 0, Option.some(new ColoredPiece(PieceType.KING, Color.WHITE)));
		setField(5, 0, Option.some(new ColoredPiece(PieceType.BISHOP, Color.WHITE)));
		setField(6, 0, Option.some(new ColoredPiece(PieceType.KNIGHT, Color.WHITE)));
		setField(7, 0, Option.some(new ColoredPiece(PieceType.ROOK, Color.WHITE)));
		setField(0, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(1, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(2, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(3, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(4, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(5, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(6, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));
		setField(7, 1, Option.some(new ColoredPiece(PieceType.PAWN, Color.WHITE)));

		setField(0, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(1, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(2, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(3, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(4, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(5, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(6, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(7, 6, Option.some(new ColoredPiece(PieceType.PAWN, Color.BLACK)));
		setField(0, 7, Option.some(new ColoredPiece(PieceType.ROOK, Color.BLACK)));
		setField(1, 7, Option.some(new ColoredPiece(PieceType.KNIGHT, Color.BLACK)));
		setField(2, 7, Option.some(new ColoredPiece(PieceType.BISHOP, Color.BLACK)));
		setField(3, 7, Option.some(new ColoredPiece(PieceType.QUEEN, Color.BLACK)));
		setField(4, 7, Option.some(new ColoredPiece(PieceType.KING, Color.BLACK)));
		setField(5, 7, Option.some(new ColoredPiece(PieceType.BISHOP, Color.BLACK)));
		setField(6, 7, Option.some(new ColoredPiece(PieceType.KNIGHT, Color.BLACK)));
		setField(7, 7, Option.some(new ColoredPiece(PieceType.ROOK, Color.BLACK)));
	}
}
