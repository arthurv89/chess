package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.engine.FruitEngine;
import nl.arthurvlug.chess.engine.RybkaEngine;
import nl.arthurvlug.chess.gui.board.ClockPane;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		WhitePlayer whitePlayer = new WhitePlayer(new RybkaEngine());
		BlackPlayer blackPlayer = new BlackPlayer(new FruitEngine());
		
		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(Game.class).toInstance(new Game(whitePlayer, blackPlayer));
		bind(ClockPane.class).toInstance(new ClockPane());
	}
}
