package nl.arthurvlug.chess.utils.board.pieces;

import java.util.Optional;

public class PieceStringUtils {
	public static final PieceCharacterConverter pieceToCharacterConverter = new PieceCharacterConverter();
	public static final PieceToChessSymbolConverter pieceToChessSymbolMap = new PieceToChessSymbolConverter();

	public static String toCharacterString(final ColoredPiece coloredPiece, final PieceConverter converter) {
		return toCharacterString(coloredPiece.getPieceType(), coloredPiece.getColor(), converter);
	}

	public static String toCharacterString(final PieceType pieceType, final Color color, final PieceConverter converter) {
		return Character.toString(toCharacter(pieceType, color, converter));
	}

	private static char toCharacter(final PieceType pieceType, final Color color, final PieceConverter converter) {
		return converter.convert(pieceType, color);
	}

	public static Optional<PieceType> fromChar(final char character, final PieceConverter converter) {
		Optional<PieceType> type = converter.fromChar(character);
		if(!type.isPresent()) {
			throw new IllegalArgumentException("Could not find piece with char " + character);
		}
		return type;
	}
}
