package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.domain.game.Clock;

import com.google.inject.Inject;


public class FruitEngine extends AbstractEngine {
	private static final String FRUIT_ENGINE = "Fruit-2-3-1.exe";

	@Inject
	public FruitEngine(Clock whiteClock, Clock blackClock) {
		super(FRUIT_ENGINE, whiteClock, blackClock);
	}

	@Override
	public String getName() {
		return "Fruit 2.3.1";
	}
}
