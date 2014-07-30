package nl.arthurvlug.chess.domain.game;

import com.google.common.collect.ImmutableList;

import rx.Observable;

public interface Player {
	String getName();

	Observable<Move> registerMoveSubscriber();

	void notifyNewMove(ImmutableList<Move> moves);
}
