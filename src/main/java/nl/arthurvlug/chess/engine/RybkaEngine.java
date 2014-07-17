package nl.arthurvlug.chess.engine;

import com.google.inject.Inject;

public class RybkaEngine extends AbstractEngine {
	private static final String RYBKA_PATH = "Rybkav2.3.2a.mp.x64.exe";
	
	@Inject
	public RybkaEngine() {
		super(RYBKA_PATH);
	}
}
