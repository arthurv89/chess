package nl.arthurvlug.chess.engine.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.StringToBoardConverter;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;

public class ACEBoardUtils {
	public static ACEBoard initializedBoard(final Color toMoveColor, String board) {
		final ACEBoard startPositionBoard = ACEBoard.emptyBoard(toMove(toMoveColor), false);
		StringToBoardConverter.conv(board, ((coordinates, coloredPiece) -> {
			startPositionBoard.addPiece(toMove(coloredPiece.getColor()), coloredPiece.getPieceType(), FieldUtils.fieldIdx(coordinates));
			return null;
		}));
		startPositionBoard.finalizeBitboards();
		return startPositionBoard;
	}

	public static int toMove(final Color toMoveColor) {
		return toMoveColor.isWhite() ? 0 : 1;
	}

	public static String dump(final ACEBoard aceBoard) {
		final Class<ACEBoard> aceBoardClass = ACEBoard.class;
		return Arrays.stream(aceBoardClass.getFields())
				.map(f -> fieldString(aceBoard, aceBoardClass, f))
				.collect(Collectors.joining("\n"));
	}

	private static String fieldString(final ACEBoard aceBoard, final Class<ACEBoard> aceBoardClass, final Field f) {
		try {
			final String fieldName = f.getName();
			final Field field = aceBoardClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			String value = field.get(aceBoard).toString();
			return String.format("%s=%s", fieldName, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
