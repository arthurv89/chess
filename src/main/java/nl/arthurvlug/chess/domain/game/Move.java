package nl.arthurvlug.chess.domain.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.domain.board.Coordinates;

@AllArgsConstructor
@Getter
public class Move {
	private Coordinates from;
	private Coordinates to;
}
