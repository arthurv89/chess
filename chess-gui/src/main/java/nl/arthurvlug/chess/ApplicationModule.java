package nl.arthurvlug.chess;

import nl.arthurvlug.chess.domain.game.Clock;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.player.BlackPlayer;
import nl.arthurvlug.chess.domain.game.player.WhitePlayer;
import nl.arthurvlug.chess.engine.binary.FruitEngine;
import nl.arthurvlug.chess.engine.customEngine.ace.ACE;
import nl.arthurvlug.chess.gui.board.ClockPane;
import nl.arthurvlug.chess.gui.board.MovesPane;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		Clock whiteClock = new Clock(0, 5);
		Clock blackClock = new Clock(0, 5);

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
