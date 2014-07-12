package nl.arthurvlug.chess.domain.game;

import nl.arthurvlug.chess.events.MoveEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public abstract class Player {
	@Inject
	private EventBus eventBus;
	
	protected void createMove(Player player) {
		if(player == this) {
			Move move = findMove();
			eventBus.post(new MoveEvent(move));
		}
	}

	protected abstract Move findMove();
}
