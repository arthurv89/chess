package nl.arthurvlug.chess.domain.pieces;

import lombok.Getter;

public enum PieceType {
//	public static final Piece PAWN = new Pawn();
//	public static final Piece KNIGHT = new Knight();
//	public static final Piece BISHOP = new Bishop();
//	public static final Piece ROOK = new Rook();
//	public static final Piece QUEEN = new Queen();
//	public static final Piece KING = new King();
	
	PAWN(new Pawn()), KNIGHT(new Knight()), BISHOP(new Bishop()), ROOK(new Rook()), QUEEN(new Queen()), KING(new King());
	
	@Getter
	private final Piece piece;

	private PieceType(Piece piece) {
		this.piece = piece;
	}
	
	public static PieceType fromChar(char character) {
		for(PieceType pieceType : values()) {
			if(Character.toLowerCase(pieceType.getPiece().getCharacter()) == Character.toLowerCase(character)) {
				return pieceType;
			}
		}
		throw new IllegalArgumentException("Could not find piece with char " + character);
	}
}
