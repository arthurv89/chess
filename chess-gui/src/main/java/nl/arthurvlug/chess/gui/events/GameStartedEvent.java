package nl.arthurvlug.chess.gui.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.gui.game.Game;

@AllArgsConstructor
@Value
@Getter
public class GameStartedEvent {
	private final Game game;
}
