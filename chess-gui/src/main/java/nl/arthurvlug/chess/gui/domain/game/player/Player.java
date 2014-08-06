package nl.arthurvlug.chess.gui.domain.game.player;

import nl.arthurvlug.chess.utils.domain.game.Move;
import rx.Observable;

import com.google.common.collect.ImmutableList;

public interface Player {
	String getName();

	Observable<Move> registerMoveSubscriber();

	void notifyNewMove(ImmutableList<Move> moves);
}
