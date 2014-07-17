package nl.arthurvlug.chess.gui.board;

import lombok.AllArgsConstructor;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.engine.AbstractEngine;
import rx.Observable;

@AllArgsConstructor
public class ComputerPlayer extends Player {
	private final AbstractEngine engine;
	
	public Observable<String> getEngineOutput(){
		return engine.subscribeEngineOutput();
	}
	
	@Override
	public Observable<Move> registerMoveSubscriber() {
		return engine.registerMoveSubscriber();
	}

	public void startEngine() {
		engine.startEngine();
	}
	
	@Override
	public void determineNextMove(Game game) {
		engine.determineNextMove(game);
	}
}
