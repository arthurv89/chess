package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.utils.board.pieces.Color;

public class EngineConstants {
	public final static int WHITE = 0;
	public final static int BLACK = 1;
	
	public static int fromColor(Color color) {
		return color == Color.WHITE ? WHITE : BLACK;
	}
}
