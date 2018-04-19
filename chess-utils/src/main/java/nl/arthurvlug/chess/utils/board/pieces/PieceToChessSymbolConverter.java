package nl.arthurvlug.chess.utils.board.pieces;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class PieceToChessSymbolConverter extends PieceConverter {
	private static final BiMap<PieceType, PieceSymbol> map = ImmutableBiMap.<PieceType, PieceSymbol> builder()
			.put(PieceType.PAWN, new PieceSymbol('♙', '♟'))
			.put(PieceType.KNIGHT, new PieceSymbol('♘', '♞'))
			.put(PieceType.BISHOP, new PieceSymbol('♗', '♝'))
			.put(PieceType.ROOK, new PieceSymbol('♖', '♜'))
			.put(PieceType.QUEEN, new PieceSymbol('♕', '♛'))
			.put(PieceType.KING, new PieceSymbol('♔', '♚'))
			.build();

	@Override
	public BiMap<PieceType, PieceSymbol> getMap() {
		return map;
	}
}
