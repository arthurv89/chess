package nl.arthurvlug.chess.utils.board.pieces;

import com.google.common.collect.BiMap;
import java.util.Optional;

abstract class PieceConverter {
	Optional<PieceType> fromChar(final char character) {
		return getMap()
				.values()
				.stream()
				.filter(pieceSymbol -> pieceSymbol.getWhite() == character || pieceSymbol.getBlack() == character)
				.findFirst()
				.map(pieceSymbol -> getMap().inverse().get(pieceSymbol));
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
