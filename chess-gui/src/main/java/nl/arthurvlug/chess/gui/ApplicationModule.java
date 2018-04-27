package nl.arthurvlug.chess.gui;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
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
	private int computerTime = 8; // in minutes
	private Color computerColor = Color.WHITE;

	@Override
	protected void configure() {
		EventBus eventBus = new EventBus("Default eventbus");
		Game game = createOnlineSyncGame(computerColor, eventBus);
//		Game game = aceVsAceGame(eventBus);

		bind(EventBus.class).toInstance(eventBus);
		bind(Game.class).toInstance(game);
		bind(ClockPane.class).toInstance(new ClockPane());
		bind(MovesPane.class).toInstance(new MovesPane());
	}

	private Game createOnlineSyncGame(final Color acePlayer, final EventBus eventBus) {
		if(acePlayer == Color.WHITE) {
			return aceWhiteOnlineSyncGame(eventBus);
		} else {
			return aceBlackOnlineSyncGame(eventBus);
		}
	}

	private Game aceWhiteOnlineSyncGame(final EventBus eventBus) {
		Clock whiteClock = new Clock(computerTime, 0);
		Clock blackClock = new Clock(100, 0);

		Player whitePlayer = createComputerPlayer(whiteClock, eventBus);
		Player blackPlayer = new HumanPlayer();

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

	private ComputerPlayer createComputerPlayer(final Clock clock, final EventBus eventBus) {
		return new ComputerPlayer(new ACE(Integer.MAX_VALUE, (int) clock.getRemainingTime().getMillis(), "ACE", eventBus));
	}

	private Game aceBlackOnlineSyncGame(final EventBus eventBus) {
		Clock whiteClock = new Clock(100, 0);
		Clock blackClock = new Clock(computerTime, 0);

		Player whitePlayer = new HumanPlayer();
		Player blackPlayer = createComputerPlayer(blackClock, eventBus);

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

		Player whitePlayer = new ComputerPlayer(new ACE(6, (int) whiteClock.getRemainingTime().getMillis(), "WHITE", eventBus));
		Player blackPlayer = new ComputerPlayer(new ACE(6, (int) blackClock.getRemainingTime().getMillis(), "BLACK", eventBus));

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

}
