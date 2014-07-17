package nl.arthurvlug.chess.domain.pieces;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ColoredPiece {
	@Getter
	private final Piece piece;
	@Getter
	private final Color color;
	
	public String getCharacterString() {
		String characterString = Character.toString(piece.getCharacter());
		if(color == Color.WHITE) {
			return characterString.toLowerCase();
		} else {
			return characterString.toUpperCase();
		}
	}
	
	@Override
	public String toString() {
		return color.toString() + " " + piece.getClass().getSimpleName();
	}
}
