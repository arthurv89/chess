package nl.arthurvlug.chess.domain.game;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.BlackPlayer;
import nl.arthurvlug.chess.WhitePlayer;
import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.pieces.InitialBoard;
import nl.arthurvlug.chess.engine.GameFinished;
import nl.arthurvlug.chess.engine.Markers;
import nl.arthurvlug.chess.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.GameFinishedEvent;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.events.StartupEvent;
import nl.arthurvlug.chess.gui.board.ComputerPlayer;
import rx.observers.EmptyObserver;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@EventHandler
@Slf4j
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
	private final Clock whiteClock = new Clock(0, 5);
	@Getter
	private final Clock blackClock = new Clock(0, 5);
	

	private volatile boolean gameFinished = false;

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
		startClockMonitor();
		this.whiteClock.startClock();
		toMove.determineNextMove(this);
	}

	private void startClockMonitor() {
		new Thread(new Runnable() {
			public void run() {
				while(!gameFinished) {
					if(whiteClock.isTimeUp() || blackClock.isTimeUp()) {
						finishGame();
					}
				}
			}
		}).start();
	}

	private void startEngine(final ComputerPlayer player) {
		player.startEngine();
		player.registerMoveSubscriber().subscribe(new EmptyObserver<Move>() {
			public void onNext(Move move) {
				getToMoveClock().stopClock();
				if(move instanceof GameFinished) {
					finishGame();
				}
				
				if(!gameFinished) {
					log.debug(Markers.ENGINE, "Applying move");
					applyMove(move);
				}
			}

			@Override
			public void onError(Throwable e) {
				log.error(Markers.GAME, "Unknown error", e);
			}

			@Override
			public void onCompleted() {
				log.error(Markers.ENGINE, "Completed!");
			}
		});
	}
	
	private void finishGame() {
		log.info(Markers.GAME, "Game has finished!");
		gameFinished = true;
		whiteClock.stopClock();
		blackClock.stopClock();
		eventBus.post(new GameFinishedEvent(Game.this));
	}

	private void applyMove(Move move) {
		// Apply move
		board.move(move);
		moves.add(move);
		toMove = other(toMove);
		eventBus.post(new MoveAppliedEvent());
		toMove.determineNextMove(this);

		getToMoveClock().startClock();
	}

	private Player other(Player toMove) {
		return toMove == whitePlayer ? blackPlayer : whitePlayer;
	}

	public Clock getToMoveClock() {
		return toMove == whitePlayer ? whiteClock : blackClock;
	}
}
