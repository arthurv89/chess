package nl.arthurvlug.chess.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RybkaEngine implements Engine {
	private static final String RYBKA_PATH = "/Rybkav2.3.2a.mp.x64.exe";

	public RybkaEngine() {
		try {
			String resourcePath = getClass().getResource(RYBKA_PATH).getFile().toString();
			String command = "wine " + resourcePath;
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
			p.destroy();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	@Override
	public void nextMove() {

	}
}
