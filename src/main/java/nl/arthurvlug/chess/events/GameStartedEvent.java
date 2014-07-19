package nl.arthurvlug.chess.events;

import nl.arthurvlug.chess.domain.game.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor
@Value
@Getter
public class GameStartedEvent {
	private final Game game;
}
