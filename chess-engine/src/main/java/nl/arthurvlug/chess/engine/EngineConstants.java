package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.utils.board.pieces.Color;

public class EngineConstants {
	public final static byte WHITE = 0;
	public final static byte BLACK = 1;
	
	public static int fromColor(Color color) {
		return color == Color.WHITE ? WHITE : BLACK;
	}
}
