package nl.arthurvlug.chess.gui.domain.game.player;

import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.gui.components.board.ComputerPlayer;

public class WhitePlayer extends ComputerPlayer {
	public WhitePlayer(UCIEngine engine) {
		super(engine);
	}
}
