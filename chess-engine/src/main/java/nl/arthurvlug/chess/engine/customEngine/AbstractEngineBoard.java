package nl.arthurvlug.chess.engine.customEngine;

import java.util.List;

public abstract class AbstractEngineBoard {
	public abstract List<Integer> generateTakeMoves();

	public abstract List<Integer> generateMoves();

	public abstract int getRepeatedMove();

	public abstract int getFiftyMove();

	public abstract int getToMove();

	public abstract boolean hasNoKing();

	public abstract int getZobristHash();

	public abstract void unapply(final Integer move);
}
