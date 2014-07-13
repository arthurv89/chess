package nl.arthurvlug.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.engine.RybkaEngine;
import nl.arthurvlug.chess.events.EventHandler;
import rx.Observable;

import com.google.inject.Inject;

@EventHandler
public class WhitePlayer extends Player {
	@Inject
	private RybkaEngine engine;

	@Override
	protected Observable<Move> findMove() {
		return engine.nextMove();
	}

	public WhitePlayer() {
		super();
		new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
				
				String line;
				try {
					while((line = d.readLine()) != null) {
						engine.sendCommand(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
