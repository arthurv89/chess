package nl.arthurvlug.chess.gui.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.game.Move;

@AllArgsConstructor
@Getter
public class MoveAppliedEvent {
	private final Move move;
}
