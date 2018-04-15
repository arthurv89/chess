package nl.arthurvlug.chess.engine;

public class ColorUtils {
	public static byte otherToMove(byte toMove) {
		return toMove == EngineConstants.WHITE
				? EngineConstants.BLACK
				: EngineConstants.WHITE;
	}

	public static boolean isWhite(int toMove) {
		return toMove == EngineConstants.WHITE;
	}

}
