package nl.arthurvlug.chess.engine;

import com.google.inject.Inject;


public class FruitEngine extends AbstractEngine {
	private static final String FRUIT_ENGINE = "Fruit-2-3-1.exe";

	@Inject
	public FruitEngine() {
		super(FRUIT_ENGINE);
	}
}
