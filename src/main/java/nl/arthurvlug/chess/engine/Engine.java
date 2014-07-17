package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.domain.game.Move;
import rx.Observable;

public interface Engine {
	Observable<Move> registerMoveSubscriber();
}
