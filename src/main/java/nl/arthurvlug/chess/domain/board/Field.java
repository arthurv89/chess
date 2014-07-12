package nl.arthurvlug.chess.domain.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.domain.pieces.ColoredPiece;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
public class Field {
	private final Coordinates coordinates;
	
	@Setter
	private Option<ColoredPiece> piece;

	public Field(int x, int y, Option<ColoredPiece> piece) {
		this.coordinates = new Coordinates(x, y);
		this.piece = piece;
	}
}
