package nl.arthurvlug.chess.gui.game.player;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

@AllArgsConstructor
public class ComputerPlayer extends Player {
	private final UCIEngine engine;

	@Override
	public Observable<Void> initialize(Clock whiteClock, Clock blackClock) {
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
