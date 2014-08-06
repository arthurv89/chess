package nl.arthurvlug.chess.engine.customEngine;

import java.util.List;

import nl.arthurvlug.chess.domain.board.pieces.Color;

public class EngineBoard {
	public Color toMove;
	public int minValue;

	public EngineBoard(List<String> moveList) {
		apply(moveList);
	}

	public EngineBoard(int minValue) {
		this.minValue = minValue;
	}

	private void apply(List<String> moveList) {
		throw new UnsupportedOperationException();
		
	}
}
