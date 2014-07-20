package nl.arthurvlug.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.Piece;
import nl.arthurvlug.chess.domain.pieces.PieceType;

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
	private BufferedReader error;
	
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
				log.error("Unknown error", e);
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
				try {
					long whiteMillis = game.getWhiteClock().getRemainingTime().getMillis();
					long blackMillis = game.getBlackClock().getRemainingTime().getMillis();
					if(whiteMillis > 0 && blackMillis > 0) {
						sendCommand(
								"position moves " + MoveUtils.toEngineMoves(game.getMoves()) + "\n" +
								"go wtime " + whiteMillis + " btime " + blackMillis);
					}
				} catch(Exception e) {
					log.error("Unknown error", e);
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
						while((line = reader.readLine()) != null) {
							parseLine(line, printStream);
						}
					}
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}).start();
	}

	private void parseLine(String line, PrintStream printStream) {
		if(line.startsWith("bestmove")) {
			StringTokenizer tokenizer = new StringTokenizer(line);
			
			// Skip first word
			tokenizer.nextToken();
			
			// Second word: the move
			String y = tokenizer.nextToken();
			Coordinates from = toField(y.substring(0, 2));
			Coordinates to = toField(y.substring(2, 4));
			Option<Piece> promotionPiece = y.length() == 5
					? Option.<Piece> some(PieceType.fromChar(y.charAt(4)).getPiece())
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
		printStream.println(line);

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
		String resourcePath = getClass().getResource("/engines/" + fileName).getFile().toString();
		String command = "wine64 " + resourcePath;
		p = Runtime.getRuntime().exec(command);
	}

	public void sendCommand(String command) {
		try {
			input.write(command + "\n");
			input.flush();
			log.info(Markers.ENGINE, getClass().getSimpleName() + " - Sent command: " + command);
		} catch (IOException e) {
			log.error("Unknown error", e);
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
