package nl.arthurvlug.chess.domain.game;

import rx.Observable;

public abstract class Player {
	protected abstract Observable<Move> registerMoveSubscriber();

	public abstract void determineNextMove(Game game);
}
