package nl.arthurvlug.chess.engine.ace.board;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.utils.StringToBoardConverter;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;

public class ACEBoardUtils {
	public static ACEBoard initializedBoard(final Color toMoveColor, String board) {
		assert 64 == board.replace("\n", "").length();
		final ACEBoard startPositionBoard = ACEBoard.emptyBoard(toMove(toMoveColor), false);
		StringToBoardConverter.conv(board, ((coordinates, coloredPiece) -> {
			startPositionBoard.addPiece(toMove(coloredPiece.getColor()), coloredPiece.getPieceType(), FieldUtils.fieldIdx(coordinates));
			return null;
		}));
		startPositionBoard.finalizeBitboards();
		return startPositionBoard;
	}

	private static byte toMove(final Color toMoveColor) {
		return (byte) (toMoveColor.isWhite() ? 0 : 1);
	}

	public static String stringDump(final ACEBoard engineBoard) {
		return dumper(engineBoard, field -> true);
	}

	public static String threeFoldRepetitionDump(final ACEBoard engineBoard) {
		return dumper(engineBoard, field ->
						!field.getName().equals("plyStack") &&
						!field.getName().equals("fiftyMove"));
	}

	private static String dumper(final ACEBoard engineBoard, final Predicate<? super Field> fieldPredicate) {
		final Class<ACEBoard> engineBoardClass = ACEBoard.class;
		return Arrays.stream(engineBoardClass.getDeclaredFields())
				.filter(fieldPredicate)
				.map(f -> fieldString(engineBoard, engineBoardClass, f))
				.collect(Collectors.joining("\n"));
	}

	private static String fieldString(final ACEBoard engineBoard, final Class<ACEBoard> engineBoardClass, final Field f) {
		try {
			final String fieldName = f.getName();
			final Field field = engineBoardClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			final Object o = field.get(engineBoard);
			String value = o.toString();
			if(o.getClass().isArray()) {
				if(o instanceof long[]) {
					value = Arrays.toString((long[]) o);
				} else if(o instanceof short[]) {
					value = Arrays.toString((short[]) o);
				} else if(o instanceof int[][]) {
					value = Arrays.deepToString((int[][]) o);
				} else {
					throw new RuntimeException("Could not convert " + o.getClass());
				}
			}
			return String.format("%s=%s", fieldName, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
