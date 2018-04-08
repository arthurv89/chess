package nl.arthurvlug.chess.engine.customEngine.movegeneration;

import java.util.Optional;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import com.atlassian.fugue.Option;


public class BitboardUtils {
	public static int fieldIdx(final Coordinates coordinates) {
		return coordinates.getX() + coordinates.getY()*8;
	}

	public static Coordinates coordinates(int i) {
		return new Coordinates(i%8, i/8);
	}

	public static Move move(String move) {
		String from = move.substring(0, 2);
		String to = move.substring(2);
		// TODO: Implement promotion
		return new Move(coordinates(from), coordinates(to), Optional.empty());
	}

	public static Coordinates coordinates(String field) {
		int x = field.charAt(0) - 'a';
		int y = field.charAt(1) - '1';
		return new Coordinates(x, y);
	}

	public static String toBitboardString(long bitboard) {
		String paddedBinaryString = "";
		String binaryString = Long.toBinaryString(bitboard);
		for (int i = 0; i < 64 - binaryString.length(); i++) {
			paddedBinaryString += "0";
		}
		paddedBinaryString += binaryString;
		
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
		return resultingString.toString();
	}

	public static int toIndex(String fieldName) {
		return fieldIdx(coordinates(fieldName));
	}

	public static long bitboardFromString(String fieldName) {
		return 1L << toIndex(fieldName);
	}
}
