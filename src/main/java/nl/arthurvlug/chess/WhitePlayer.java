package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.engine.RybkaEngine;
import nl.arthurvlug.chess.events.AskForMoveEvent;
import nl.arthurvlug.chess.events.EventHandler;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@EventHandler
public class WhitePlayer extends Player {
	@Inject
	private RybkaEngine engine;
	
	@Subscribe
	public void on(AskForMoveEvent event) {
		createMove(event.getPlayer());
	}
	
	@Override
	protected Move findMove() {
		engine.nextMove();
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Coordinates from = new Coordinates(1, 1);
		Coordinates to = new Coordinates(4, 4);
		return new Move(from, to);
	}
}
