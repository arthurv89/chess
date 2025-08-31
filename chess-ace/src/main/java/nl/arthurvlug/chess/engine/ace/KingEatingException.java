package nl.arthurvlug.chess.engine.ace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.MoveUtils;

@AllArgsConstructor
@Getter
public class KingEatingException extends Exception {
	private int move;

	@Override
	public String toString() {
		return "KingEatingException(move=%s)".formatted(UnapplyableMoveUtils.toString(move));
	}
}
