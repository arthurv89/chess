package nl.arthurvlug.chess.events;

import lombok.AllArgsConstructor;
import lombok.Value;
import nl.arthurvlug.chess.domain.game.Move;

@AllArgsConstructor
@Value
public class MoveEvent {
	private Move move;
}
