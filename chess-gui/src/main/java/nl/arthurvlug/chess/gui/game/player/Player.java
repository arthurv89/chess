package nl.arthurvlug.chess.gui.game.player;

import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

import com.google.common.collect.ImmutableList;

public abstract class Player {
	public abstract String getName();

	public abstract Observable<Move> registerMoveSubscriber();

	public abstract Observable<Void> initialize(Clock whiteClock, Clock blackClock);

	public abstract void notifyNewMove(ImmutableList<Move> moves);

	public abstract void stop();
}
