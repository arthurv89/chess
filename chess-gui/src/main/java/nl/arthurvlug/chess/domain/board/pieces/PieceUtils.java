package nl.arthurvlug.chess.domain.board.pieces;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

public class PieceUtils {
	private static Map<PieceType, Character> pieceCharacterMap = ImmutableMap.<PieceType, Character> builder()
			.put(PieceType.PAWN, 'p')
			.put(PieceType.KNIGHT, 'n')
			.put(PieceType.BISHOP, 'l')
			.put(PieceType.ROOK, 'r')
			.put(PieceType.QUEEN, 'q')
			.put(PieceType.KING, 'k')
			.build();

	public static String toCharacterString(ColoredPiece coloredPiece) {
		return toCharacterString(coloredPiece.getPieceType(), coloredPiece.getColor());
	}

	public static String toCharacterString(PieceType pieceType, Color color) {
		String characterString = Character.toString(pieceCharacterMap.get(pieceType));
		if(color == Color.WHITE) {
			return characterString.toUpperCase();
		} else {
			return characterString.toLowerCase();
		}
	}

	public static PieceType fromChar(char character) {
		for(Entry<PieceType, Character> pieceCharacterEntry : pieceCharacterMap.entrySet()) {
			if(Character.toLowerCase(pieceCharacterEntry.getValue()) == Character.toLowerCase(character)) {
				return pieceCharacterEntry.getKey();
			}
		}
		throw new IllegalArgumentException("Could not find piece with char " + character);
	}
}
