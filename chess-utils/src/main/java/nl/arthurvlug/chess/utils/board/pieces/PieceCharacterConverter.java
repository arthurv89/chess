package nl.arthurvlug.chess.utils.board.pieces;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class PieceCharacterConverter extends PieceConverter<Character> {
	private static Map<PieceType, Character> map = ImmutableMap.<PieceType, Character> builder()
			.put(PieceType.PAWN, 'p')
			.put(PieceType.KNIGHT, 'n')
			.put(PieceType.BISHOP, 'l')
			.put(PieceType.ROOK, 'r')
			.put(PieceType.QUEEN, 'q')
			.put(PieceType.KING, 'k')
			.build();

	@Override
	boolean isPiece(Character pieceCharater, char inputCharacter) {
		return pieceCharater.charValue() == Character.toLowerCase(inputCharacter);
	}

	@Override
	Map<PieceType, Character> getMap() {
		return map;
	}

	@Override
	char whiteChar(Character c) {
		return Character.toUpperCase(c);
	}

	@Override
	char blackChar(Character c) {
		return c;
	}
}
