package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.events.BoardVisible;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.ShutdownEvent;

import com.google.common.eventbus.Subscribe;

@EventHandler
public class RybkaEngine extends AbstractEngine {
	private static final String RYBKA_PATH = "Rybkav2.3.2a.mp.x64.exe";

	public RybkaEngine() {
		super(RYBKA_PATH);
	}
	
	@Subscribe
	public void on(BoardVisible event) {
		startEngine();
	}

	@Subscribe
	public void on(ShutdownEvent event) {
		handleShutdownEvent();
	}
}
