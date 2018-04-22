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
	@Override
	protected void configure() {
		Game game = createOnlineSyncGame(Color.BLACK);
//		Game game = aceVsAceGame();

		bind(EventBus.class).toInstance(new EventBus("Default eventbus"));
		bind(Game.class).toInstance(game);
		bind(ClockPane.class).toInstance(new ClockPane());
		bind(MovesPane.class).toInstance(new MovesPane());
	}

	private Game createOnlineSyncGame(final Color acePlayer) {
		if(acePlayer == Color.WHITE) {
			return aceWhiteOnlineSyncGame();
		} else {
			return aceBlackOnlineSyncGame();
		}
	}

	private Game aceWhiteOnlineSyncGame() {
		Clock whiteClock = new Clock(20, 0);
		Clock blackClock = new Clock(100, 30);

		Player whitePlayer = createComputerPlayer(whiteClock);
		Player blackPlayer = new HumanPlayer();

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

	private ComputerPlayer createComputerPlayer(final Clock clock) {
		return new ComputerPlayer(new ACE(Integer.MAX_VALUE, (int) clock.getRemainingTime().getMillis()));
	}

	private Game aceBlackOnlineSyncGame() {
		Clock whiteClock = new Clock(100, 30);
		Clock blackClock = new Clock(20, 0);

		Player whitePlayer = new HumanPlayer();
		Player blackPlayer = createComputerPlayer(blackClock);

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

	private Game aceVsAceGame() {
		Clock whiteClock = new Clock(0, 15);
		Clock blackClock = new Clock(0, 15);

		Player whitePlayer = new ComputerPlayer(new ACE(6, (int) whiteClock.getRemainingTime().getMillis()));
		Player blackPlayer = new ComputerPlayer(new ACE(6, (int) blackClock.getRemainingTime().getMillis()));

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

}
