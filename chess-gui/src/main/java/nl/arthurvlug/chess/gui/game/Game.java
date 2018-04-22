package nl.arthurvlug.chess.gui.game;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.gui.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.gui.events.EventHandler;
import nl.arthurvlug.chess.gui.events.GameFinishedEvent;
import nl.arthurvlug.chess.gui.events.GameStartedEvent;
import nl.arthurvlug.chess.gui.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.events.StartupEvent;
import nl.arthurvlug.chess.gui.game.player.Player;
import nl.arthurvlug.chess.utils.Markers;
import nl.arthurvlug.chess.utils.MyEmptyObserver;
import nl.arthurvlug.chess.utils.NamedThread;
import nl.arthurvlug.chess.utils.board.Board;
import nl.arthurvlug.chess.utils.board.InitialBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.game.GameFinished;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

@EventHandler
@Slf4j
public class Game {
	@Inject private EventBus eventBus;

	@Getter private final Board board;
	
	@Getter private final Player whitePlayer;
	@Getter private final Player blackPlayer;

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

	public ImmutableList<Move> immutableMoveList() {
		return ImmutableList.copyOf(moves);
	}

	@Subscribe
	public void on(StartupEvent event) {
		eventBus.post(new GameStartedEvent(this));
	}

	@Subscribe
	public void on(BoardWindowInitializedEvent event) throws InterruptedException {
		Observable<Void> whitePlayingEngineObservable = whitePlayer.initialize(whiteClock, blackClock);
		Observable<Void> blackPlayingEngineObservable = blackPlayer.initialize(whiteClock, blackClock);

		final long initialWhiteTime = whiteClock.getRemainingTime().getMillis();
		final long initialClockTime = blackClock.getRemainingTime().getMillis();
		subscribeToMove(whitePlayer);
		subscribeToMove(blackPlayer);
		
		Observable.merge(whitePlayingEngineObservable, blackPlayingEngineObservable).subscribe(new MyEmptyObserver<Void>() {
			@Override
			public void onCompleted() {
				startClockMonitor();
				
				whitePlayer.notifyNewMove(ImmutableList.<Move> of());
				
				whiteClock.startClock();
			}
		});
	}

	private void startClockMonitor() {
		new NamedThread(new Runnable() {
			public void run() {
				while(!gameFinished) {
					if(whiteClock.isTimeUp() || blackClock.isTimeUp()) {
						log.info("Time is up! " + whiteClock.getRemainingTime().getMillis() + " vs " + blackClock.getRemainingTime().getMillis());
						finishGame();
					}
				}
			}
		}, "Chess clock monitor").start();
	}

	private void subscribeToMove(final Player player) {
		player.registerMoveSubscriber().subscribe(new MyEmptyObserver<Move>() {
			public void onNext(Move move) {
				getToMoveClock().stopClock();
				
				if(move instanceof GameFinished) {
					GameFinished gameFinishedMove = (GameFinished) move;
					applyMove(gameFinishedMove.getMove());
					finishGame();
				} else {
					applyMove(move);
					getToMoveClock().startClock();
					toMove.notifyNewMove(immutableMoveList());
				}
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
		whitePlayer.stop();
		blackPlayer.stop();
		whiteClock.stopClock();
		blackClock.stopClock();
		eventBus.post(new GameFinishedEvent(Game.this));
	}

	private void applyMove(Move move) {
		// Apply move
		board.move(move);
		moves.add(move);
		toMove = other(toMove);
		eventBus.post(new MoveAppliedEvent(move));
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
		private Player whitePlayer;
		private Player blackPlayer;
		private Player toMove;
		private Board initialBoard = InitialBoard.create();
		private Clock whiteClock;
		private Clock blackClock;

		public GameBuilder whitePlayer(Player whitePlayer) {
			this.whitePlayer = whitePlayer;
			return this;
		}

		public GameBuilder blackPlayer(Player blackPlayer) {
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
