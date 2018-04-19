package nl.arthurvlug.chess.utils.board.pieces;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

class PieceCharacterConverter extends PieceConverter {
	private static BiMap<PieceType, PieceSymbol> map = ImmutableBiMap.<PieceType, PieceSymbol> builder()
			.put(PieceType.PAWN, new PieceSymbol('P', 'p'))
			.put(PieceType.KNIGHT, new PieceSymbol('N', 'n'))
			.put(PieceType.BISHOP, new PieceSymbol('L', 'l'))
			.put(PieceType.ROOK, new PieceSymbol('R', 'r'))
			.put(PieceType.QUEEN, new PieceSymbol('Q', 'q'))
			.put(PieceType.KING, new PieceSymbol('K', 'k'))
			.build();

	@Override
	BiMap<PieceType, PieceSymbol> getMap() {
		return map;
	}
}
