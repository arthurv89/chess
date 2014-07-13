package nl.arthurvlug.chess.engine;

import rx.Observable;
import nl.arthurvlug.chess.domain.game.Move;

public interface Engine {
	Observable<Move> nextMove();
}
