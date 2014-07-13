package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.engine.FruitEngine;
import nl.arthurvlug.chess.engine.RybkaEngine;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(Game.class).toInstance(new Game());
		bind(WhitePlayer.class).toInstance(new WhitePlayer());
		bind(BlackPlayer.class).toInstance(new BlackPlayer());
		bind(FruitEngine.class).toInstance(new FruitEngine());
		bind(RybkaEngine.class).toInstance(new RybkaEngine());
	}
}
