package nl.arthurvlug.chess.gui;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import nl.arthurvlug.chess.engine.ace.ACE;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.gui.components.board.ClockPane;
import nl.arthurvlug.chess.gui.components.board.MovesPane;
import nl.arthurvlug.chess.gui.game.Game;
import nl.arthurvlug.chess.gui.game.player.ComputerPlayer;
import nl.arthurvlug.chess.gui.game.player.HumanPlayer;
import nl.arthurvlug.chess.gui.game.player.Player;
import nl.arthurvlug.chess.utils.board.pieces.Color;

public class ApplicationModule extends AbstractModule {
	private int computerTime = 10; // in minutes
	private Color computerColor = Color.BLACK;

	@Override
	protected void configure() {
		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(ClockPane.class).toInstance(new ClockPane());
		bind(MovesPane.class).toInstance(new MovesPane());
	}

	@Provides
	public Game createGame(final EventBus eventBus) {
		final Game game = createOnlineSyncGame(eventBus);
		game.setEventBus(eventBus);
		return game;
	}

	private Game createOnlineSyncGame(final EventBus eventBus) {
		if(computerColor == Color.WHITE) {
			return aceWhiteOnlineSyncGame(eventBus);
		} else {
			return aceBlackOnlineSyncGame(eventBus);
		}
	}

	private Game aceWhiteOnlineSyncGame(final EventBus eventBus) {
		Clock whiteClock = new Clock(computerTime, 0);
		Clock blackClock = new Clock(100, 0);

		Player whitePlayer = createComputerPlayer(whiteClock, blackClock, eventBus);
		Player blackPlayer = new HumanPlayer();

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

	private ComputerPlayer createComputerPlayer(final Clock whiteClock, final Clock blackClock, final EventBus eventBus) {
		return new ComputerPlayer(new ACE(Integer.MAX_VALUE, "ACE", eventBus));
	}

	private Game aceBlackOnlineSyncGame(final EventBus eventBus) {
		Clock whiteClock = new Clock(100, 0);
		Clock blackClock = new Clock(computerTime, 0);

		Player whitePlayer = new HumanPlayer();
		Player blackPlayer = createComputerPlayer(whiteClock, blackClock, eventBus);

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

	private Game aceVsAceGame(final EventBus eventBus) {
		Clock whiteClock = new Clock(computerTime, 0);
		Clock blackClock = new Clock(computerTime, 0);

		Player whitePlayer = new ComputerPlayer(new ACE(6, "WHITE", eventBus));
		Player blackPlayer = new ComputerPlayer(new ACE(6, "BLACK", eventBus));

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}
}
