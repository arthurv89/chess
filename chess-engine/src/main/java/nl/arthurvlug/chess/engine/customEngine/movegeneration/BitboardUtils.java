package nl.arthurvlug.chess.engine.customEngine.movegeneration;

import java.util.Optional;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.game.Move;

import static nl.arthurvlug.chess.utils.StringToBoardConverter.conv;
import static nl.arthurvlug.chess.utils.board.FieldUtils.coordinates;


public class BitboardUtils {
	public static Move move(String move) {
		String from = move.substring(0, 2);
		String to = move.substring(2);
		// TODO: Implement promotion
		return new Move(coordinates(from), coordinates(to), Optional.empty());
	}

	public static String targetBitboardString(long bitboard) {
		StringBuilder paddedBinaryString = new StringBuilder();
		String binaryString = Long.toBinaryString(bitboard);
		for (int i = 0; i < 64 - binaryString.length(); i++) {
			paddedBinaryString.append("0");
		}
		paddedBinaryString.append(binaryString);
		
		StringBuilder resultingString = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			StringBuilder rowBuilder = new StringBuilder();
			for (int j = 0; j < 8; j++) {
				int idx = 8*i + j;
				rowBuilder.append(paddedBinaryString.charAt(idx));
			}
			rowBuilder = rowBuilder.reverse();
			rowBuilder.append('\n');
			resultingString.append(rowBuilder);
		}
		return resultingString.toString().replace("0", ".").replace("1", "♟");
	}

	public static long bitboardFromFieldName(String fieldNames) {
		final String[] fieldNameArray = fieldNames.split(" ");
		long bitboard = 0L;
		for(final String fieldName : fieldNameArray) {
			bitboard |= 1L << FieldUtils.fieldIdx(fieldName);
		}
		return bitboard;
	}

	public static long bitboardFromBoard(final String board) {
		final long[] bitboard = {0L};
		conv(board, ((coordinates, coloredPiece) -> {
			bitboard[0] |= 1L << FieldUtils.fieldIdx(coordinates);
			return null;
		}));
		return bitboard[0];
	}
}
