package nl.arthurvlug.chess.domain.pieces;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ColoredPiece {
	private final Piece piece;
	private final Color color;
	
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
