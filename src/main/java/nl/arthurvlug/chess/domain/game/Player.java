package nl.arthurvlug.chess.domain.game;

import rx.Observable;

import com.google.common.collect.ImmutableList;

public interface Player {
	String getName();

	Observable<Move> registerMoveSubscriber();

	void notifyNewMove(ImmutableList<Move> moves);
}
