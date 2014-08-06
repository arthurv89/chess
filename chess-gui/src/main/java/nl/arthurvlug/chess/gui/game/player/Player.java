package nl.arthurvlug.chess.gui.game.player;

import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

import com.google.common.collect.ImmutableList;

public interface Player {
	String getName();

	Observable<Move> registerMoveSubscriber();

	void notifyNewMove(ImmutableList<Move> moves);
}
