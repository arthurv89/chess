package nl.arthurvlug.chess.gui.board;

import lombok.AllArgsConstructor;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.Player;
import nl.arthurvlug.chess.engine.AbstractEngine;
import rx.Observable;

import com.google.common.collect.ImmutableList;

@AllArgsConstructor
public abstract class ComputerPlayer implements Player {
	protected final AbstractEngine engine;
	
	public Observable<String> getEngineOutput(){
		return engine.subscribeEngineOutput();
	}
	
	@Override
	public Observable<Move> registerMoveSubscriber() {
		return engine.registerMoveSubscriber();
	}

	public Observable<Void> startEngine(Game game) {
		return engine.startEngine(game);
	}
	
//	@Override
//	public void think(List<Move> moves) {
//		engine.think(ImmutableList.<Move> builder().addAll(moves).build());
//	}

	@Override
	public String getName() {
		return engine.getClass().getSimpleName();
	}
	
	@Override
	public void notifyNewMove(ImmutableList<Move> moves) {
		engine.notifyNewMove(moves);
	}
}
