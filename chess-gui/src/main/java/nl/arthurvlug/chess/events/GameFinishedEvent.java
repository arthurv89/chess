package nl.arthurvlug.chess.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.domain.game.Game;

@Getter
@AllArgsConstructor
public class GameFinishedEvent {
	private Game game;
}
