package nl.arthurvlug.chess.engine.customEngine;

import java.util.List;
import nl.arthurvlug.chess.utils.game.Move;

public abstract class AbstractEngineBoard<T extends AbstractEngineBoard<T>> {
	public abstract List<Move> generateMoves();

	public abstract Move getLastMove();

	public abstract int getRepeatedMove();

	public abstract int getFiftyMove();

	public abstract boolean opponentIsInCheck(final List<Move> generatedMoves);

	/**
	 * @deprecated Use createMoves instead (in combination with apply/unapply)
	 */
	@Deprecated
	public abstract List<T> generateSuccessorBoards(final List<Move> generatedMoves);

	/**
	 * @deprecated Use createMoves instead (in combination with apply/unapply)
	 */
	@Deprecated
	public abstract List<T> generateSuccessorTakeBoards();

	public abstract int getToMove();

	public abstract boolean hasNoKing();
}
