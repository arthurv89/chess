package nl.arthurvlug.chess.gui.events;

import lombok.AllArgsConstructor;
import lombok.Value;
import nl.arthurvlug.chess.utils.domain.game.Move;

@AllArgsConstructor
@Value
public class MoveEvent {
	private Move move;
}
