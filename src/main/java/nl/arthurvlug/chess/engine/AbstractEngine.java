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
import nl.arthurvlug.chess.MyThread;
import nl.arthurvlug.chess.domain.game.Clock;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.game.MyEmptyObserver;

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
	private volatile Subscriber<? super Void> engineStartedSubscriber;
	private final Set<Subscriber<? super Void>> engineStopSubscribers = new HashSet<>();

	// Process
	private Process p;
	private BufferedReader output;
	private BufferedWriter input;
	private BufferedReader error;

	// Current state
	private boolean started = false;
	private volatile String ponderMove;
	private volatile boolean shouldIgnoreNextMove = false;
	
	// Game
	private final Clock whiteClock;
	private final Clock blackClock;
	private volatile ImmutableList<Move> gameMoves = ImmutableList.<Move> of();

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
					log.error(Markers.ENGINE, getName() + " -    Unknown error", e);
					throw new RuntimeException(e);
				}
			}
		});
	}

	private void handleBestMove(String line) {
		final StringTokenizer tokenizer = new StringTokenizer(line);

		tokenizer.nextToken(); // Skip "bestmove"
		final String sMove = tokenizer.nextToken();
		final Move move = MoveUtils.toMove(sMove);

		if(tokenizer.hasMoreTokens()) {
			if(!shouldIgnoreNextMove) {
				tokenizer.nextToken(); // Skip "ponder"
	
				// Think with position after the bestMove and the ponderMove
				String ponderMove = tokenizer.nextToken();
				ponder(move, ponderMove);
	
				for (Subscriber<? super Move> moveSubscriber : moveSubscribers) {
					log.debug(Markers.ENGINE, getName() + " -    Notifying listener for move " + move);
					moveSubscriber.onNext(move);
				}
			}
		} else {
			for (Subscriber<? super Move> moveSubscriber : moveSubscribers) {
				log.debug(Markers.ENGINE, getName() + " -    Game finished!");
				moveSubscriber.onNext(new GameFinished(move));
			}
		}
		
		for (Subscriber<? super Void> engineStopSubscriber : engineStopSubscribers) {
			engineStopSubscriber.onCompleted();
		}
	}

	private void ponder(Move lastMove, String ponderMove) {
		log.debug(Markers.ENGINE, getName() + " -    Ponder: wait");
		synchronized (gameMoves) {
			this.ponderMove = ponderMove;
			final ImmutableList<Move> ponderMoves = ImmutableList.<Move> builder()
					.addAll(gameMoves)
					.add(lastMove)
					.add(MoveUtils.toMove(ponderMove))
					.build();
			log.debug(Markers.ENGINE, getName() + " -    Pondering after " + ponderMoves);
			
			// Change the position
			sendCommand("position moves " + MoveUtils.toEngineMoves(ponderMoves));
			
			// Think
			final long whiteMillis = whiteClock.getRemainingTime().getMillis();
			final long blackMillis = blackClock.getRemainingTime().getMillis();
			sendCommand("go ponder wtime " + whiteMillis + " btime " + blackMillis);
			log.debug(Markers.ENGINE, getName() + " -    Ponder: Out of synchronized");
		}
	}

	public void notifyNewMove(final ImmutableList<Move> moves) {
		if(ponderMove == null) {
			think(moves);
		} else {
			synchronized (gameMoves) {
				gameMoves = moves;
				Move lastMove = moves.get(moves.size()-1);
				if(ponderMove.equals(lastMove.toString())) {
					shouldIgnoreNextMove = false;
					ponderMove = null;
					engineStopSubscribers.remove(AbstractEngine.this);
					sendCommand("ponderhit");
				} else { 
					restartEngine(moves);
				}
			}
		}
	}

	private void restartEngine(final ImmutableList<Move> moves) {
		stopEngine().subscribe(new MyEmptyObserver<Void>() {
			@Override
			public void onCompleted() {
				shouldIgnoreNextMove = false;
				engineStopSubscribers.remove(AbstractEngine.this);
				think(moves);
			}
		});
	}
	
	private void think(ImmutableList<Move> moves) {
		log.debug(Markers.ENGINE, getName() + " -    Thinking after moves " + moves.toString());
		synchronized (gameMoves) {
//			log.debug(Markers.ENGINE, getName() + " -    Think: In synchronized");
			gameMoves = moves;
			
			// Change the position
			sendCommand("position moves " + MoveUtils.toEngineMoves(gameMoves));
		}
			
		// Think
		final long whiteMillis = whiteClock.getRemainingTime().getMillis();
		final long blackMillis = blackClock.getRemainingTime().getMillis();
		sendCommand("go wtime " + whiteMillis + " btime " + blackMillis);
	}

	private Observable<Void> stopEngine() {
		return Observable.create(new OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> engineStopSubscriber) {
				shouldIgnoreNextMove = true;
				engineStopSubscribers.add(engineStopSubscriber);
				sendCommand("stop");
			}
		});
	}

	
	

	

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
		log.debug(Markers.ENGINE_RAW, getName() + " -             " + line);
		
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

		runOutputThread(output, "output", System.out);
		runOutputThread(error, "error", System.err);
	}

	private void runOutputThread(final BufferedReader reader, String streamName, final PrintStream printStream) {
		new MyThread(new Runnable() {
			public void run() {
				try {
					while (true) {
						String line;
						while ((line = reader.readLine()) != null) {
							parseLine(line, printStream);
						}
					}
				} catch (IOException e) {
					log.error(Markers.ENGINE, getName() + " -    " + e.getMessage());
				}
			}
		}, "Engine " + getName() + " - " + streamName).start();
	}

	public void sendCommand(String command) {
		try {
			input.write(command + "\n");
			input.flush();
			log.info(Markers.ENGINE, getName() + " -    Sent command: " + command);
		} catch (IOException e) {
			log.error(Markers.ENGINE, getName() + " -    Unknown error", e);
			e.printStackTrace();
		}
	}

	protected void handleShutdownEvent() {
		p.destroy();
		IOUtils.closeQuietly(output);
		IOUtils.closeQuietly(input);
		log.info(Markers.ENGINE, getName() + " -    Engine down");
	}

	public abstract String getName();
}
