package nl.arthurvlug.chess.engine.customEngine.movegeneration;

import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import com.atlassian.fugue.Option;


public class BitboardUtils {
	public static int toIndex(final Coordinates coordinates) {
		return coordinates.getX() + coordinates.getY()*8;
	}

	public static Coordinates coordinates(int i) {
		return new Coordinates(i%8, i/8);
	}

	public static Move move(String move) {
		String from = move.substring(0, 2);
		String to = move.substring(2);
		// TODO: Implement promotion
		return new Move(coordinates(from), coordinates(to), Option.<PieceType> none());
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
		for (int j = 0; j < 64; j++) {
			resultingString.append(paddedBinaryString.charAt(j));
			if((j+1) % 8 == 0) {
				resultingString.append('\n');
			}
		}
		return resultingString.toString();
	}

	public static int toIndex(String fieldName) {
		return toIndex(coordinates(fieldName));
	}
}
