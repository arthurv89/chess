package nl.arthurvlug.chess.domain.game.player;

import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.gui.board.ComputerPlayer;

public class WhitePlayer extends ComputerPlayer {
	public WhitePlayer(UCIEngine engine) {
		super(engine);
	}
}
