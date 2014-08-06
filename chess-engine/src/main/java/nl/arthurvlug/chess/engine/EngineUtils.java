package nl.arthurvlug.chess.engine;

public class EngineUtils {
	public static int otherToMove(int toMove) {
		return toMove == EngineConstants.WHITE
				? EngineConstants.BLACK
				: EngineConstants.WHITE;
	}

	public static boolean isWhite(int toMove) {
		return toMove == EngineConstants.WHITE;
	}

	public static boolean isBlack(int toMove) {
		return toMove == EngineConstants.BLACK;
	}

}
