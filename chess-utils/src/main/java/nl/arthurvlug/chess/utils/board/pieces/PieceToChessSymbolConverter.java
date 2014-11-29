package nl.arthurvlug.chess.utils.board.pieces;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class PieceToChessSymbolConverter extends PieceConverter<PieceSymbol> {
	private static final Map<PieceType, PieceSymbol> map = ImmutableMap.<PieceType, PieceSymbol> builder()
			.put(PieceType.PAWN, new PieceSymbol('♙', '♟'))
			.put(PieceType.KNIGHT, new PieceSymbol('♘', '♞'))
			.put(PieceType.BISHOP, new PieceSymbol('♗', '♝'))
			.put(PieceType.ROOK, new PieceSymbol('♖', '♜'))
			.put(PieceType.QUEEN, new PieceSymbol('♕', '♛'))
			.put(PieceType.KING, new PieceSymbol('♔', '♚')).build();

	@Override
	public char convert(final PieceType pieceType, final Color color) {
		final PieceSymbol pieceSymbol = map.get(pieceType);
		if(color.isWhite()) {
			return pieceSymbol.getWhite();
		} else {
			return pieceSymbol.getBlack();
		}
	}

	@Override
	boolean pred(PieceSymbol symbol, char character) {
		return symbol.getWhite() == character || symbol.getBlack() == character;
	}

	@Override
	Map<PieceType, PieceSymbol> getMap() {
		return map;
	}
}
