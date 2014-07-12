package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.events.AskForMoveEvent;
import nl.arthurvlug.chess.events.EventHandler;

import com.google.common.eventbus.Subscribe;

@EventHandler
public class BlackPlayer extends Player {
	@Subscribe
	public void on(AskForMoveEvent event) {
		createMove(event.getPlayer());
	}
	
	@Override
	protected Move findMove() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Coordinates from = new Coordinates(6, 1);
		Coordinates to = new Coordinates(2, 4);
		return new Move(from, to);
	}
}
