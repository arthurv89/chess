package nl.arthurvlug.chess.utils.board.pieces;

import com.google.common.collect.BiMap;
import java.util.Optional;

public abstract class PieceConverter {
	public Optional<PieceType> pieceTypeFromChar(final char character) {
		return getMap()
				.values()
				.stream()
				.filter(pieceSymbol -> pieceSymbol.getWhite() == character || pieceSymbol.getBlack() == character)
				.findFirst()
				.map(pieceSymbol -> getMap().inverse().get(pieceSymbol));
	}

	public Optional<ColoredPiece> coloredPieceFromChar(final char character) {
		return getMap()
				.values()
				.stream()
				.flatMap(pieceSymbol -> {
					boolean isWhite = pieceSymbol.getWhite() == character;
					boolean isBlack = pieceSymbol.getBlack() == character;
					if(isWhite || isBlack) {
						final PieceType pieceType = getMap().inverse().get(pieceSymbol);
						final ColoredPiece value = new ColoredPiece(pieceType, isWhite ? Color.WHITE : Color.BLACK);
						return Optional.of(value).stream();
					}
					return Optional.<ColoredPiece> empty().stream();
				})
				.findFirst();
	}

	char convert(final PieceType pieceType, final Color color) {
		PieceSymbol t = getMap().get(pieceType);
		if(color == Color.WHITE) {
			return t.getWhite();
		} else {
			return t.getBlack();
		}
	}

	abstract BiMap<PieceType, PieceSymbol> getMap();
}
