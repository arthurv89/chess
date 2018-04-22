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
		Game game = createOnlineSyncGame(Color.WHITE);

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
		Clock whiteClock = new Clock(8, 30);
		Clock blackClock = new Clock(12, 30);

		Player whitePlayer = new ComputerPlayer(new ACE(5));
		Player blackPlayer = new HumanPlayer();

		return new Game.GameBuilder()
			.whitePlayer(whitePlayer)
			.blackPlayer(blackPlayer)
			.whiteClock(whiteClock)
			.blackClock(blackClock)
			.toMove(whitePlayer)
			.build();
	}

	private Game aceBlackOnlineSyncGame() {
		Clock whiteClock = new Clock(12, 30);
		Clock blackClock = new Clock(8, 30);

		Player whitePlayer = new HumanPlayer();
		Player blackPlayer = new ComputerPlayer(new ACE(5));

		return new Game.GameBuilder()
				.whitePlayer(whitePlayer)
				.blackPlayer(blackPlayer)
				.whiteClock(whiteClock)
				.blackClock(blackClock)
				.toMove(whitePlayer)
				.build();
	}

}
