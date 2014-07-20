package nl.arthurvlug.chess.domain.pieces;

import lombok.Getter;

public class ColoredPiece {
	@Getter
	private final Piece piece;
	@Getter
	private final Color color;

	public ColoredPiece(PieceType pieceType, Color color) {
		this(pieceType.getPiece(), color);
	}
	
	public ColoredPiece(Piece piece, Color color) {
		this.piece = piece;
		this.color = color;
	}

	public String getCharacterString() {
		String characterString = Character.toString(piece.getCharacter());
		if(color == Color.WHITE) {
			return characterString.toUpperCase();
		} else {
			return characterString.toLowerCase();
		}
	}
	
	@Override
	public String toString() {
		return color.toString() + " " + piece.getClass().getSimpleName();
	}
}
