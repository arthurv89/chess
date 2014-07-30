package nl.arthurvlug.chess.engine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.domain.game.Clock;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;

import org.apache.commons.io.IOUtils;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.google.common.collect.ImmutableList;

@Slf4j
public abstract class AbstractEngine implements Engine {
	// Engine
	private final String fileName;
	
	// Observers
	private final Set<Subscriber<? super Move>> moveSubscribers = new HashSet<>();
	private final Set<Subscriber<? super String>> engineOutputSubscribers = new HashSet<>();
	private Subscriber<? super Void> engineStartedSubscriber;
	private final Set<Subscriber<? super Void>> engineStopSubscribers = new HashSet<>();

	// Process
	private Process p;
	private BufferedReader output;
	private BufferedWriter input;
	private BufferedReader error;

	// Current state
	private boolean started = false;
	private volatile String ponderMove;
	
	// Game
	private final Clock whiteClock;
	private final Clock blackClock;
	private ImmutableList<Move> gameMoves = ImmutableList.<Move> of();

	public AbstractEngine(final String fileName, Clock whiteClock, Clock blackClock) {
		this.fileName = checkNotNull(fileName);
		this.whiteClock = checkNotNull(whiteClock);
		this.blackClock = checkNotNull(blackClock);
	}

	public synchronized Observable<Void> startEngine(final Game game) {
		if (started) {
			throw new RuntimeException();
		}
		started = true;

		return Observable.create(new OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				try {
					engineStartedSubscriber = subscriber;
					startProcess();
					processOutput();
					sendCommand("uci");
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					log.error(Markers.ENGINE, "Unknown error", e);
					throw new RuntimeException(e);
				}
			}
		});
	}

//	public void startThinking() {
//		sendCommand("go"
//				+ " wtime " + whiteClock.getRemainingTime().getMillis()
//				+ " btime " + blackClock.getRemainingTime().getMillis());
//	}

	private void handleBestMove(String line) {
		final StringTokenizer tokenizer = new StringTokenizer(line);

		tokenizer.nextToken(); // Skip "bestmove"
		final String sMove = tokenizer.nextToken();
		final Move move = MoveUtils.toMove(sMove);

		if(tokenizer.hasMoreTokens()) {
			tokenizer.nextToken(); // Skip "ponder"

		// Think with position after the bestMove and the ponderMove
//		String ponderMove = tokenizer.nextToken();
//		ponder(ponderMove, move);
		}

		for (Subscriber<? super Move> moveSubscriber : moveSubscribers) {
			log.info(Markers.ENGINE, getClass().getSimpleName() + " - Notifying listener for move " + move);
			if (move.getFrom().equals(move.getTo())) {
				moveSubscriber.onNext(new GameFinished());
			} else {
				moveSubscriber.onNext(move);
			}
		}
		
		for (Subscriber<? super Void> engineStopSubscriber : engineStopSubscribers) {
			engineStopSubscriber.onCompleted();
		}
	}

	private void ponder(String ponderMove, Move move) {
		synchronized (gameMoves) {
			log.debug(Markers.ENGINE, "Ponder: in synchronized");
			this.ponderMove = ponderMove;
			final ImmutableList<Move> ponderMoves = ImmutableList.<Move> builder()
					.addAll(gameMoves)
					.add(move)
					.add(MoveUtils.toMove(ponderMove))
					.build();
			
			Set<Move> ponderMovesSet = new HashSet<Move>(ponderMoves);
			// Change the position
			if(ponderMovesSet.size() == ponderMoves.size()) {
				sendCommand("position moves " + MoveUtils.toEngineMoves(ponderMoves));
				
				// Think
				sendCommand("go ponder");
			}
			log.debug(Markers.ENGINE, "Thinkg: Out of synchronized");
		}
	}

	public void notifyNewMove(final ImmutableList<Move> moves) {
//		if(moves.size() <= 2) {
			think(moves);
//		} else {
//			stopEngine().subscribe(new MyEmptyObserver<Void>() {
//				@Override
//				public void onCompleted() {
//					engineStopSubscribers.remove(AbstractEngine.this);
//					think(moves);
//				}
//			});
//		}
	}
	
	private void think(ImmutableList<Move> moves) {
		log.debug(Markers.ENGINE, "Thinking after moves " + moves.toString());
		synchronized (gameMoves) {
			log.debug(Markers.ENGINE, "Think: In synchronized");
			gameMoves = moves;
			
			// Change the position
			sendCommand("position moves " + MoveUtils.toEngineMoves(gameMoves));
			
			// Think
			final long whiteMillis = whiteClock.getRemainingTime().getMillis();
			final long blackMillis = blackClock.getRemainingTime().getMillis();
			sendCommand("go wtime " + whiteMillis + " btime " + blackMillis);
			log.debug(Markers.ENGINE, "Think: out synchronized");
		}
	}

	private Observable<Void> stopEngine() {
		return Observable.create(new OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> engineStopSubscriber) {
				engineStopSubscribers.add(engineStopSubscriber);
				sendCommand("stop");
			}
		});
	}

//	private void think(final ImmutableList<Move> moves) {
//		final long whiteMillis = whiteClock.getRemainingTime().getMillis();
//		final long blackMillis = blackClock.getRemainingTime().getMillis();
//		if (whiteMillis > 0 && blackMillis > 0) {
//			// boolean doPonderhit = canPonder && !list.isEmpty()
//			// &&
//			// MoveUtils.toEngineMove(list.get(list.size()-1)).equals(ponderMove);
//			boolean doPonderHit = false;
//			if (doPonderHit) {
//				log.info(Markers.ENGINE, getClass().getSimpleName() + " - " + ponderMove);
//				ignoreOutput = false;
//				log.info(Markers.ENGINE, getClass().getSimpleName() + " - Ignore output: " + ignoreOutput);
//
//				sendCommand("ponderhit");
//			} else {
//				// TODO: Ignore next bestmove
////				ignoreOutput = canPonder;
//				log.info(Markers.ENGINE, getClass().getSimpleName() + " - Ignore output: " + ignoreOutput);
//
//				sendCommand("stop");
//
//				sendCommand("position moves " + MoveUtils.toEngineMoves(moves));
//			}
//
//			if (moves.size() < 2) {
//				sendCommand("go wtime " + whiteMillis + " btime " + blackMillis);
//			} else {
//				sendCommand("go ponder wtime " + whiteMillis + " btime " + blackMillis);
//			}
//		}
//	}
	
	
	

	

	public Observable<Move> registerMoveSubscriber() {
		return Observable.create(new OnSubscribe<Move>() {
			@Override
			public void call(Subscriber<? super Move> subscriber) {
				moveSubscribers.add(subscriber);
			}
		});
	};

	public Observable<String> subscribeEngineOutput() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				engineOutputSubscribers.add(subscriber);
			}
		});
	};
	
	
	
	
	
	

	private void parseLine(final String line, final PrintStream printStream) {
		if (!line.isEmpty() && !line.startsWith("info ")) {
			log.debug(Markers.ENGINE, getClass().getSimpleName() + " - Parsing line '" + line + "'");
			// printStream.println(line);
		}
		
		if (line.equals("uciok")) {
			engineStartedSubscriber.onCompleted();
		} else if (line.startsWith("bestmove")) {
			handleBestMove(line);
		}

		for (Subscriber<? super String> engineSubscriber : engineOutputSubscribers) {
			engineSubscriber.onNext(line);
		}
	}

	private void startProcess() throws IOException {
		final String resourcePath = getClass().getResource("/engines/" + fileName).getFile().toString();
		final String command = "wine64 " + resourcePath;
		p = Runtime.getRuntime().exec(command);
	}

	private void processOutput() throws InterruptedException, IOException {
		output = new BufferedReader(new InputStreamReader(p.getInputStream()));
		error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		input = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

		runOutputThread(output, System.out);
		runOutputThread(error, System.err);
	}

	private void runOutputThread(final BufferedReader reader, final PrintStream printStream) {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						String line;
						while ((line = reader.readLine()) != null) {
							parseLine(line, printStream);
							Thread.sleep(10);
						}
					}
				} catch (IOException | InterruptedException e) {
					log.error(Markers.ENGINE, e.getMessage());
				}
			}
		}).start();
	}

	public void sendCommand(String command) {
		try {
			input.write(command + "\n");
			input.flush();
			log.info(Markers.ENGINE, getClass().getSimpleName() + " - Sent command: " + command);
		} catch (IOException e) {
			log.error(Markers.ENGINE, "Unknown error", e);
			e.printStackTrace();
		}
	}

	protected void handleShutdownEvent() {
		p.destroy();
		IOUtils.closeQuietly(output);
		IOUtils.closeQuietly(input);
		log.info(Markers.ENGINE, "Engine down");
	}

	public abstract String getName();
}
