package nl.arthurvlug.chess.gui.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.gui.domain.game.player.Player;

@AllArgsConstructor
@Value
public class AskForMoveEvent {
	@Getter
	private Player player;
}
