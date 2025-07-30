package nl.arthurvlug.chess.engine.ace.board;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.StringToBoardConverter;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

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
		return Arrays.stream(ACEBoard.class.getDeclaredFields())
				.filter(fieldPredicate)
				.map(f -> Optional.ofNullable(fieldString(engineBoard, ACEBoard.class, f)))
				.flatMap(x -> x.stream())
				.collect(Collectors.joining("\n"));
	}

	private static String fieldString(final ACEBoard engineBoard, final Class<ACEBoard> engineBoardClass, final Field f) {
		try {
			final String fieldName = f.getName();
			if(fieldName.equals("Companion")) {
				return null;
			}

			final Field field = engineBoardClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			final Object o = field.get(engineBoard);
			String value = o.toString();
			if(o instanceof Long l) {
				value = longToBinaryLineString(l);
			} else if(o.getClass().isArray()) {
				if(o instanceof long[] arr) {
					value = Arrays.toString(arr);
				} else if(o instanceof short[] arr) {
					value = Arrays.toString(arr);
				} else if(o instanceof int[][] arr) {
					value = Arrays.deepToString(arr);
				} else if(o instanceof byte[] arr) {
					value = "\n" + piecesToString(arr);
				} else {
					throw new RuntimeException("Could not convert " + o.getClass());
				}
			}
			return String.format("%s=%s", fieldName, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static String piecesToString(byte[] arr) {
		return IntStream.iterate(7, i -> i >= 0, i -> i - 1)
				.mapToObj(i -> IntStream.range(0, 8)
						.mapToObj(j -> Optional.ofNullable(ColoredPieceType.from(arr[i * 8 + j]))
								.map(ColoredPiece::getCharacterString)
								.orElse("."))
						.collect(Collectors.joining()))
				.collect(Collectors.joining("\n"));
	}

	private static String longToBinaryLineString(Long l) {
		return "\n" + BitboardUtils.toString(l);
	}
}
