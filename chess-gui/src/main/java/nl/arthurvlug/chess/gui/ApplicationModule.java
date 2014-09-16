package nl.arthurvlug.chess.gui;

import nl.arthurvlug.chess.engine.ace.ACE;
import nl.arthurvlug.chess.engine.binary.FruitEngine;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.gui.components.board.ClockPane;
import nl.arthurvlug.chess.gui.components.board.MovesPane;
import nl.arthurvlug.chess.gui.game.Game;
import nl.arthurvlug.chess.gui.game.player.BlackPlayer;
import nl.arthurvlug.chess.gui.game.player.WhitePlayer;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		Clock whiteClock = new Clock(10, 0);
		Clock blackClock = new Clock(1, 0);

//		WhitePlayer whitePlayer = new WhitePlayer(new RybkaEngine());
		WhitePlayer whitePlayer = new WhitePlayer(new ACE());
		BlackPlayer blackPlayer = new BlackPlayer(new FruitEngine());
		Game game = new Game.GameBuilder()
			.whitePlayer(whitePlayer)
			.blackPlayer(blackPlayer)
			.whiteClock(whiteClock)
			.blackClock(blackClock)
			.toMove(whitePlayer)
			.build();

		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(Game.class).toInstance(game);
		bind(ClockPane.class).toInstance(new ClockPane());
		bind(MovesPane.class).toInstance(new MovesPane());
	}
}
