package nl.arthurvlug.chess.utils.board;

import nl.arthurvlug.chess.utils.MoveUtils;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class Coordinates {
	private final int x;
	private final int y;
	
	public String toString() {
		return MoveUtils.fieldToString(this);
	}
}
