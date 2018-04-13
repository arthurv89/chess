package nl.arthurvlug.chess.utils.board;

public class FieldUtils {
	public static Coordinates coordinates(int i) {
		return new Coordinates(i % 8, i / 8);
	}

	public static int fieldIdx(final Coordinates coordinates) {
		return coordinates.getX() + coordinates.getY()*8;
	}

	public static Coordinates coordinates(String field) {
		int x = field.charAt(0) - 'a';
		int y = field.charAt(1) - '1';
		return new Coordinates(x, y);
	}
}
