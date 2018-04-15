package nl.arthurvlug.chess.utils.board;

public class FieldUtils {
	public static Coordinates coordinates(int i) {
		return new Coordinates(i % 8, i / 8);
	}

	public static int fieldIdx(final Coordinates coordinates) {
		return coordinates.getX() + coordinates.getY()*8;
	}

	public static int fieldIdx(String fieldName) {
		return fieldIdx(coordinates(fieldName));
	}

	public static Coordinates coordinates(String field) {
		int x = field.charAt(0) - 'a';
		int y = field.charAt(1) - '1';
		return new Coordinates(x, y);
	}

	private static String toReadableField(Coordinates from) {
		return Character.toString((char) (from.getX() + 'a'))
				+ Integer.toString(from.getY() + 1);
	}

	public static String fieldToString(Coordinates coordinates) {
		return toReadableField(coordinates);
	}
}
