package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.engine.FruitEngine;
import nl.arthurvlug.chess.events.EventHandler;
import rx.Observable;

import com.google.inject.Inject;

@EventHandler
public class BlackPlayer extends Player {
	@Inject
	private FruitEngine engine;
	
	@Override
	protected Observable<Move> findMove() {
		return engine.nextMove();
	}
}
