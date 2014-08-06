package nl.arthurvlug.chess.util;

import lombok.extern.slf4j.Slf4j;
import rx.observers.EmptyObserver;

@Slf4j
public class MyEmptyObserver<T> extends EmptyObserver<T> {
	@Override
	public void onError(Throwable e) {
		log.error(Markers.GAME, "Unknown error", e);
	}
}
