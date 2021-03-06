package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

public interface Engine {
	Observable<Move> registerMoveSubscriber();
	
	String getName();
}
