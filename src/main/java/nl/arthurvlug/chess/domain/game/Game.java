package nl.arthurvlug.chess.domain.game;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.BlackPlayer;
import nl.arthurvlug.chess.WhitePlayer;
import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.pieces.Color;
import nl.arthurvlug.chess.domain.pieces.InitialBoard;
import nl.arthurvlug.chess.engine.GameFinished;
import nl.arthurvlug.chess.engine.Markers;
import nl.arthurvlug.chess.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.GameFinishedEvent;
import nl.arthurvlug.chess.events.GameStartedEvent;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.events.StartupEvent;
import rx.Observable;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@EventHandler
@Slf4j
public class Game {
	@Inject private EventBus eventBus;

	@Getter private final Board board;
	
	@Getter private final WhitePlayer whitePlayer;
	@Getter private final BlackPlayer blackPlayer;

	@Getter private final Clock whiteClock;
	@Getter private final Clock blackClock;

	@Getter private Player toMove;
	private final List<Move> moves = new ArrayList<Move>();
	private volatile boolean gameFinished = false;
	

	public Game(GameBuilder gameBuilder) {
		this.whitePlayer = checkNotNull(gameBuilder.whitePlayer);
		this.blackPlayer = checkNotNull(gameBuilder.blackPlayer);
		this.whiteClock = checkNotNull(gameBuilder.whiteClock);
		this.blackClock = checkNotNull(gameBuilder.blackClock);
		this.toMove = checkNotNull(gameBuilder.toMove);
		this.board = checkNotNull(gameBuilder.initialBoard);
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
		Observable<Void> whitePlayingEngineObservable = whitePlayer.startEngine(this);
		Observable<Void> blackPlayingEngineObservable = blackPlayer.startEngine(this);

		subscribeToMove(whitePlayer);
		subscribeToMove(blackPlayer);
		
		Observable.merge(whitePlayingEngineObservable, blackPlayingEngineObservable).subscribe(new MyEmptyObserver<Void>() {
//		whitePlayingEngineObservable.subscribe(new MyEmptyObserver<Void>() {
			@Override
			public void onCompleted() {
				startClockMonitor();
				
				whitePlayer.notifyNewMove(ImmutableList.<Move> of());
				
				whiteClock.startClock();

//				log.debug("Think: " + color(toMove));
//				toMove.think(ImmutableList.<Move> of());
			}
		});
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

	private void subscribeToMove(final Player player) {
		player.registerMoveSubscriber().subscribe(new MyEmptyObserver<Move>() {
			public void onNext(Move move) {
				getToMoveClock().stopClock();
//				log.debug("Time remaining: " + whiteClock.getRemainingTime().getSecondOfMinute() + " - " + blackClock.getRemainingTime().getSecondOfMinute());
				
//				log.debug(Markers.ENGINE, player.getName() + " is applying move " + move);
				applyMove(move);
				
				if(move instanceof GameFinished) {
					finishGame();
				}
				
//				if(!gameFinished) {
//				}
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
		
//		log.debug("Think: " + getToMove().getName());
		toMove.notifyNewMove(ImmutableList.<Move> builder().addAll(moves).build());

		getToMoveClock().startClock();
	}

	private Color color(Player player) {
		return toMove == whitePlayer
				? Color.WHITE
				: Color.BLACK;
	}

	private Player other(Player toMove) {
		return color(toMove) == Color.WHITE
				? blackPlayer
				: whitePlayer;
	}

	public Clock getToMoveClock() {
		return color(toMove) == Color.WHITE
				? whiteClock
				: blackClock;
	}
	
	public static class GameBuilder {
		private WhitePlayer whitePlayer;
		private BlackPlayer blackPlayer;
		private Player toMove;
		private Board initialBoard = new InitialBoard();
		private Clock whiteClock;
		private Clock blackClock;

		public GameBuilder whitePlayer(WhitePlayer whitePlayer) {
			this.whitePlayer = whitePlayer;
			return this;
		}

		public GameBuilder blackPlayer(BlackPlayer blackPlayer) {
			this.blackPlayer = blackPlayer;
			return this;
		}

		public GameBuilder toMove(Player toMove) {
			this.toMove = toMove;
			return this;
		}

		public GameBuilder initialBoard(Board initialBoard) {
			this.initialBoard = initialBoard;
			return this;
		}

		public GameBuilder whiteClock(Clock whiteClock) {
			this.whiteClock = whiteClock;
			return this;
		}

		public GameBuilder blackClock(Clock blackClock) {
			this.blackClock = blackClock;
			return this;
		}

		public Game build() {
			return new Game(this);
		}
	}
}
