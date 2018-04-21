package nl.arthurvlug.chess.engine.ace;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KingEatingException extends Exception {
	private int move;
}
