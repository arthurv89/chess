package nl.arthurvlug.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.events.ShutdownEvent;

import org.apache.commons.io.IOUtils;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public abstract class AbstractEngine implements Engine {
	private Process p;
	private BufferedReader output;
	private BufferedWriter input;
	private final String fileName;
	
	private final List<Subscriber<? super Move>> subscribers = new ArrayList<Subscriber<? super Move>>();

	public AbstractEngine(String fileName) {
		this.fileName = fileName;
	}
	
	protected void startEngine() {
		try {
			startProcess();
			processOutput();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected Observable<Move> bestMove = Observable.create(new OnSubscribe<Move>() {
		@Override
		public void call(Subscriber<? super Move> subscriber) {
			subscribers.add(subscriber);
		}
	});
	
	public Observable<Move> nextMove() {
		return bestMove;
	};
	
	
	
	
	
	

	private void processOutput() throws InterruptedException, IOException {
		output = new BufferedReader(new InputStreamReader(p.getInputStream()));
		input = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

		runOutputThread();
		
		sendCommand("uci");

		sendCommand("go ponder");
		
		Thread.sleep(3000);
		sendCommand("stop");
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
					e.printStackTrace();
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
			Move move = new Move(from, to);

			for(Subscriber<? super Move> subscriber : subscribers) {
				subscriber.onNext(move);
				subscriber.onCompleted();
			}
		}
		
//		System.out.println(Thread.currentThread().getId() + ": "+ line);
		
	}
	
	private Coordinates toField(String substring) {
		int x = substring.charAt(0) - 'a';
		int y = substring.charAt(1) - '1';
		return new Coordinates(x, y);
	}

	private void startProcess() throws IOException {
		String resourcePath = getClass().getResource("/" + fileName).getFile().toString();
		String command = "wine " + resourcePath;
		p = Runtime.getRuntime().exec(command);
	}

	public void sendCommand(String command) {
		try {
			input.write(command + "\n");
			input.flush();
			System.err.println(Thread.currentThread().getName() + " - Sending command: " + command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void handleShutdownEvent() {
		p.destroy();
		IOUtils.closeQuietly(output);
		IOUtils.closeQuietly(input);
		System.out.println("Engine down");
	}

	public abstract void on(ShutdownEvent event);
}
