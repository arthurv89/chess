package nl.arthurvlug.chess;

import nl.arthurvlug.chess.engine.AbstractEngine;
import nl.arthurvlug.chess.gui.board.ComputerPlayer;

public class BlackPlayer extends ComputerPlayer {
	public BlackPlayer(AbstractEngine engine) {
		super(engine);
	}

	@Override
	public String getName() {
		return "Computer player with engine " + engine.getName();
	}
}
