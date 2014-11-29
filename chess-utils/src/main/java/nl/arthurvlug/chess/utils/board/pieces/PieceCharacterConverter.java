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
	public char convert(PieceType pieceType, Color color) {
		return map.get(pieceType).charValue();
	}

	@Override
	boolean pred(Character pieceCharater, char inputCharacter) {
		return pieceCharater.charValue() == Character.toLowerCase(inputCharacter);
	}
	
	Map<PieceType, Character> getMap() {
		return map;
	}
}
