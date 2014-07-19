package nl.arthurvlug.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.Piece;
import nl.arthurvlug.chess.domain.pieces.Pieces;

import org.apache.commons.io.IOUtils;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.atlassian.fugue.Option;

@Slf4j
public abstract class AbstractEngine implements Engine {
	private Process p;
	private BufferedReader output;
	private BufferedWriter input;
	
	private final String fileName;
	private final List<Subscriber<? super Move>> moveSubscribers = new ArrayList<Subscriber<? super Move>>();
	private final List<Subscriber<? super String>> engineOutputSubscribers = new ArrayList<Subscriber<? super String>>();
	private boolean started = false;
	
	public AbstractEngine(String fileName) {
		this.fileName = fileName;
	}
	
	public void startEngine() {
		if(!started) {
			try {
				startProcess();
				processOutput();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		started = true;
	}

	public Observable<Move> registerMoveSubscriber() {
		return Observable.create(new OnSubscribe<Move>() {
			@Override
			public void call(Subscriber<? super Move> subscriber) {
				moveSubscribers.add(subscriber);
			}
		});
	};

	public void determineNextMove(final Game game) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendCommand("position moves " + MoveUtils.toEngineMoves(game.getMoves()));
				long whiteMillis = game.getWhiteClock().getRemainingTime().getMillis();
				long blackMillis = game.getBlackClock().getRemainingTime().getMillis();
				if(whiteMillis > 0 && blackMillis > 0) {
					sendCommand("go wtime " + whiteMillis + " btime " + blackMillis);
				}
			}
		}).start();
	}
	
	public Observable<String> subscribeEngineOutput() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				engineOutputSubscribers.add(subscriber);
			}
		});
	};
	
	
	
	
	
	

	private void processOutput() throws InterruptedException, IOException {
		output = new BufferedReader(new InputStreamReader(p.getInputStream()));
		input = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

		runOutputThread();
	}

	private void runOutputThread() {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						String line;
						while((line = output.readLine()) != null) {
							parseLine(line);
						}
					}
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}).start();
	}

	private void parseLine(String line) {
		if(line.startsWith("bestmove")) {
			StringTokenizer tokenizer = new StringTokenizer(line);
			
			// Skip first word
			tokenizer.nextToken();
			
			// Second word: the move
			String y = tokenizer.nextToken();
			Coordinates from = toField(y.substring(0, 2));
			Coordinates to = toField(y.substring(2, 4));
			Option<Piece> promotionPiece = y.length() == 5
					? Option.<Piece> some(Pieces.fromChar(y.charAt(4)))
					: Option.<Piece> none();
			Move move = new Move(from, to, promotionPiece);

			for(Subscriber<? super Move> moveSubscriber : moveSubscribers) {
				if(move.getFrom().equals(move.getTo())) {
					moveSubscriber.onNext(new GameFinished());
				} else {
					moveSubscriber.onNext(move);
				}
				log.info(Markers.MOVE, "Sent " + move + " to " + moveSubscriber);
			}
		}
		log.info(Markers.ENGINE, line);

		for(Subscriber<? super String> engineSubscriber : engineOutputSubscribers) {
			engineSubscriber.onNext(line);
		}
		
	}
	
	private Coordinates toField(String substring) {
		int x = substring.charAt(0) - 'a';
		int y = substring.charAt(1) - '1';
		return new Coordinates(x, y);
	}

	private void startProcess() throws IOException {
		String resourcePath = getClass().getResource("/" + fileName).getFile().toString();
		String command = "wine64 " + resourcePath;
		p = Runtime.getRuntime().exec(command);
	}

	public void sendCommand(String command) {
		try {
			input.write(command + "\n");
			input.flush();
			log.info(Markers.ENGINE, getClass().getSimpleName() + " - Sent command: " + command);
		} catch (IOException e) {
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
