package nl.arthurvlug.chess.domain.pieces;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class Piece {
	@Getter
	private final char character;
}
