package nl.arthurvlug.chess.utils.board;

public class FieldUtils {
	@Deprecated // Don't use this method at all.
	public static Coordinates coordinates(int i) {
		return new Coordinates(i % 8, i / 8);
	}

	public static Coordinates coordinates(String field) {
		int x = field.charAt(0) - 'a';
		int y = field.charAt(1) - '1';
		return new Coordinates(x, y);
	}

	public static byte fieldIdx(final Coordinates coordinates) {
		return (byte) (coordinates.getX() + coordinates.getY()*8);
	}

	public static byte fieldIdx(String fieldName) {
		return fieldIdx(coordinates(fieldName));
	}

	private static String toReadableField(Coordinates coordinates) {
		return Character.toString((char) (coordinates.getX() + 'a'))
				+ Integer.toString(coordinates.getY() + 1);
	}

	public static String fieldToString(Coordinates coordinates) {
		return toReadableField(coordinates);
	}

	public static String fieldToString(byte fieldIdx) {
		return toReadableField(coordinates(fieldIdx));
	}
}
