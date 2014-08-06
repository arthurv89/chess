package nl.arthurvlug.chess.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.domain.game.Game;

@AllArgsConstructor
@Value
@Getter
public class GameStartedEvent {
	private final Game game;
}
