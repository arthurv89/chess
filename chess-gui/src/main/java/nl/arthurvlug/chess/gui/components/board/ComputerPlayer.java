package nl.arthurvlug.chess.gui.components.board;

import lombok.AllArgsConstructor;
import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.gui.game.player.Player;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

import com.google.common.collect.ImmutableList;

@AllArgsConstructor
public abstract class ComputerPlayer implements Player {
	private final UCIEngine engine;

	public Observable<Void> startEngine(Clock whiteClock, Clock blackClock) {
		return engine.startEngine(whiteClock, blackClock);
	}

	public void stop() {
		engine.shutdown();
	}
	
	@Override
	public void notifyNewMove(ImmutableList<Move> moves) {
		engine.notifyNewMove(moves);
	}
	
	@Override
	public Observable<Move> registerMoveSubscriber() {
		return engine.registerMoveSubscriber();
	}
	
	public Observable<String> registerEngineOutputSubscriber(){
		return engine.subscribeEngineOutput();
	}

	@Override
	public String getName() {
		return engine.getClass().getSimpleName();
	}
}
