package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.engine.FruitEngine;
import nl.arthurvlug.chess.engine.RybkaEngine;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		WhitePlayer whitePlayer = new WhitePlayer(new FruitEngine());
		BlackPlayer blackPlayer = new BlackPlayer(new RybkaEngine());
		
		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(Game.class).toInstance(new Game(whitePlayer, blackPlayer));
	}
}
