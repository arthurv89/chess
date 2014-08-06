package nl.arthurvlug.chess.domain.board;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class Coordinates {
	private final int x;
	private final int y;
}
