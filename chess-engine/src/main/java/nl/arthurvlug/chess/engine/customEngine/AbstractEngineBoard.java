package nl.arthurvlug.chess.engine.customEngine;

import java.util.List;
import nl.arthurvlug.chess.utils.game.Move;

public abstract class AbstractEngineBoard<M extends EngineMove> {
	public abstract List<M> generateTakeMoves();

	public abstract List<M> generateMoves();

	public abstract int getRepeatedMove();

	public abstract int getFiftyMove();

	public abstract int getToMove();

	public abstract boolean hasNoKing();

	public abstract int getZobristHash();

	public abstract void unapply(final M move);
}
