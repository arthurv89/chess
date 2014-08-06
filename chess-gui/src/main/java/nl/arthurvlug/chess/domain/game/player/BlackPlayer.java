package nl.arthurvlug.chess.domain.game.player;

import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.gui.board.ComputerPlayer;

public class BlackPlayer extends ComputerPlayer {
	public BlackPlayer(UCIEngine engine) {
		super(engine);
	}
}
