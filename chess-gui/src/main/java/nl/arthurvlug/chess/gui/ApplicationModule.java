package nl.arthurvlug.chess.gui;

import nl.arthurvlug.chess.engine.ace.ACE;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.gui.components.board.ClockPane;
import nl.arthurvlug.chess.gui.game.player.ComputerPlayer;
import nl.arthurvlug.chess.gui.components.board.MovesPane;
import nl.arthurvlug.chess.gui.game.Game;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import nl.arthurvlug.chess.gui.game.player.HumanPlayer;
import nl.arthurvlug.chess.gui.game.player.Player;

public class ApplicationModule extends AbstractModule {
	@Override
	protected void configure() {
		Clock whiteClock = new Clock(150, 0);
		Clock blackClock = new Clock(150, 0);

		Player whitePlayer = new ComputerPlayer(new ACE(2));
//		Player whitePlayer = new HumanPlayer();
		Player blackPlayer = new ComputerPlayer(new ACE(3));
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
