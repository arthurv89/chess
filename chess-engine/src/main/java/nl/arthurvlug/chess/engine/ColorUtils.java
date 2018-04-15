package nl.arthurvlug.chess.engine;

public class ColorUtils {
	public final static byte WHITE = 0;
	public final static byte BLACK = 1;

	public static byte opponent(byte toMove) {
		return toMove == WHITE
				? BLACK
				: WHITE;
	}

	public static boolean isWhite(int toMove) {
		return toMove == WHITE;
	}

}
