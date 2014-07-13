package nl.arthurvlug.chess.domain.game;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import nl.arthurvlug.chess.BlackPlayer;
import nl.arthurvlug.chess.WhitePlayer;
import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.pieces.InitialBoard;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.events.StartupEvent;
import rx.observers.EmptyObserver;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@EventHandler
public class Game {
	@Inject
	private EventBus eventBus;
	
	@Inject
	private WhitePlayer whitePlayer;
	@Inject
	private BlackPlayer blackPlayer;
	private Player toMove;

	@Getter
	private Board board;
	
	private final List<Move> moves = new ArrayList<Move>();
	
	public List<Move> getMoves() {
		return ImmutableList.copyOf(moves);
	}

	@Subscribe
	public void on(StartupEvent event) {
		this.board = new InitialBoard();
		this.toMove = whitePlayer;

		eventBus.post(new BoardInitializedEvent(board, toMove));
		findNextMove();
	}
	
	private void findNextMove() {
		toMove.findMove().subscribe(new EmptyObserver<Move>() {
			public void onNext(Move move) {
				applyMove(move);
			}
		});
	}
	
	private void applyMove(Move move) {
		board.move(move);
		moves.add(move);
		toMove = other(toMove);
		
		eventBus.post(new MoveAppliedEvent());
	}

	private Player other(Player toMove) {
		return toMove == whitePlayer ? blackPlayer : whitePlayer;
	}
}
