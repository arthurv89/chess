package nl.arthurvlug.chess.engine.binary;


public class RybkaEngine extends BinaryEngine {
	private static final String RYBKA_PATH = "Rybkav2.3.2a.mp.x64.exe";
	
	public RybkaEngine() {
		super(RYBKA_PATH);
	}

	@Override
	public String getName() {
		return "WHITE";
	}
}
