package nl.arthurvlug.chess.utils.board;

import lombok.EqualsAndHashCode;
import lombok.Value;
import nl.arthurvlug.chess.utils.MoveUtils;

@Value
@EqualsAndHashCode
public class Coordinates {
	private final int x;
	private final int y;
	
	public String toString() {
		return MoveUtils.fieldToString(this);
	}
}
