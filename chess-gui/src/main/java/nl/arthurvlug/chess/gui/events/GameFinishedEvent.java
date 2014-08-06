package nl.arthurvlug.chess.gui.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.gui.game.Game;

@Getter
@AllArgsConstructor
public class GameFinishedEvent {
	private Game game;
}
