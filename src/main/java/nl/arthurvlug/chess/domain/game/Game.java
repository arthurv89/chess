package nl.arthurvlug.chess.domain.game;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import nl.arthurvlug.chess.BlackPlayer;
import nl.arthurvlug.chess.WhitePlayer;
import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.pieces.InitialBoard;
import nl.arthurvlug.chess.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.events.StartupEvent;
import nl.arthurvlug.chess.gui.board.ComputerPlayer;
import rx.observers.EmptyObserver;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@EventHandler
public class Game {
	@Inject
	private EventBus eventBus;
	
	@Getter
	private final WhitePlayer whitePlayer;

	@Getter
	private final BlackPlayer blackPlayer;
	
	@Getter
	private Player toMove;

	@Getter
	private Board board;
	
	private final List<Move> moves = new ArrayList<Move>();

	@Getter
	private final Clock whiteClock = new Clock(1, 0);
	@Getter
	private final Clock blackClock = new Clock(1, 0);
	
	public Game(WhitePlayer whitePlayer, BlackPlayer blackPlayer) {
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.toMove = whitePlayer;
		this.board = new InitialBoard();
	}

	public List<Move> getMoves() {
		return ImmutableList.copyOf(moves);
	}

	@Subscribe
	public void on(StartupEvent event) {
		eventBus.post(new GameStartedEvent(this));
	}
	
	@Subscribe
	public void on(BoardWindowInitializedEvent event) throws InterruptedException {
		startEngine(whitePlayer);
		startEngine(blackPlayer);
		this.whiteClock.start();
		toMove.determineNextMove(this);
	}

	private void startEngine(ComputerPlayer player) {
		player.startEngine();
		player.registerMoveSubscriber().subscribe(new EmptyObserver<Move>() {
			public void onNext(Move move) {
				applyMove(move);
			}
		});
	}
	
	private void applyMove(Move move) {
		// Stop old clock
		getToMoveClock().stop();
		
		board.move(move);
		moves.add(move);
		toMove = other(toMove);
		
		eventBus.post(new MoveAppliedEvent());
		toMove.determineNextMove(this);

		// Start new clock
		getToMoveClock().start();
	}

	private Player other(Player toMove) {
		return toMove == whitePlayer ? blackPlayer : whitePlayer;
	}

	public Clock getToMoveClock() {
		return toMove == whitePlayer ? whiteClock : blackClock;
	}
}
