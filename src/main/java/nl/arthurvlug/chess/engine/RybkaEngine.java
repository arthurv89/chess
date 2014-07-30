package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.domain.game.Clock;

import com.google.inject.Inject;

public class RybkaEngine extends AbstractEngine {
	private static final String RYBKA_PATH = "Rybkav2.3.2a.mp.x64.exe";
	
	@Inject
	public RybkaEngine(Clock whiteClock, Clock blackClock) {
		super(RYBKA_PATH, whiteClock, blackClock);
	}

	@Override
	public String getName() {
		return "Rybka";
	}
}
