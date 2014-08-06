package nl.arthurvlug.chess.engine.binary;

import com.google.inject.Inject;


public class FruitEngine extends BinaryEngine {
	private static final String FRUIT_ENGINE = "Fruit-2-3-1.exe";

	@Inject
	public FruitEngine() {
		super(FRUIT_ENGINE);
	}

	@Override
	public String getName() {
		return "    BLACK";
	}
}
