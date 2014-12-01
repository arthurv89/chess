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
			.put(PieceType.KING, new PieceSymbol('♔', '♚'))
			.build();

	@Override
	boolean isPiece(PieceSymbol symbol, char character) {
		return whiteChar(symbol) == character || blackChar(symbol) == character;
	}

	@Override
	Map<PieceType, PieceSymbol> getMap() {
		return map;
	}

	@Override
	char whiteChar(PieceSymbol symbol) {
		return symbol.getWhite();
	}

	@Override
	char blackChar(PieceSymbol symbol) {
		return symbol.getBlack();
	}
}
