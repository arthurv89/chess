package nl.arthurvlug.chess.engine.customEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.utils.NamedThread;
import nl.arthurvlug.chess.utils.game.Move;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@Slf4j
public abstract class CustomEngine extends UCIEngine {
	private PipedOutputStream commandStream;
	private BufferedReader commandReader;
	
	private PipedInputStream outputStream;
	private BufferedWriter outputWriter;
	
	private PipedInputStream errorStream;
	private BufferedWriter errorWriter;
	
	



	@Override
	protected void initializeEngine() throws IOException {
		PipedInputStream in = new PipedInputStream();
		commandReader = new BufferedReader(new InputStreamReader(in));
		commandStream = new PipedOutputStream();
		commandStream.connect(in);
		
		
		PipedOutputStream out = new PipedOutputStream();
		outputWriter = new BufferedWriter(new OutputStreamWriter(out));
		outputStream = new PipedInputStream();
		out.connect(outputStream);
		
		
		PipedOutputStream err = new PipedOutputStream();
		errorWriter = new BufferedWriter(new OutputStreamWriter(err));
		errorStream = new PipedInputStream();
		err.connect(errorStream);
		
		startEngineInputParseThread();
	}

	private void startEngineInputParseThread() throws IOException {
		new NamedThread(new Runnable() {
			private ThinkingParams thinkingParams;
			private List<String> moveList;

			@Override
			public void run() {
				while(true) {
					try {
						String s = commandReader.readLine();
						if(s == null) {
							// TODO: Fix this by making it blocking
							continue;
						}
						log.debug("received {}", s);
						System.err.println("received line: " + s);
						
						if("uci".equals(s)) {
							write("uciok");
						} else if(s.startsWith("position moves")) {
							moveList = parsePosition(s);
						} else if(s.startsWith("go")) {
							thinkingParams = parseParams(s);
							new Thread(new Runnable() {
								@Override
								public void run() {
									Move move = think(moveList, thinkingParams);
									try {
										write("bestmove " + move + " ponder e1e2");
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}).start();;
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			private ThinkingParams parseParams(String line) {
				ThinkingParams thinkingParams = new ThinkingParams();
				
				StringTokenizer tokenizer = new StringTokenizer(line);
				tokenizer.nextToken(); // Skip "go"
				
				while(tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if("wtime".equals(token)) {
						thinkingParams.setWhiteTime(Integer.parseInt(tokenizer.nextToken()));
					} else if("btime".equals(token)) {
						thinkingParams.setBlackTime(Integer.parseInt(tokenizer.nextToken()));
					} else if("ponder".equals(token)) {
						// TODO: Implement pondering
						
					} else {
						throw new RuntimeException("Could not parse token " + token);
					}
				}
				
				return thinkingParams;
			}

			private List<String> parsePosition(String line) {
				return ImmutableList.<String> copyOf(
					Splitter.on(' ').splitToList(
							line.substring("position moves".length()).trim()
					)
				);
			}
		}, "Custom Engine input parser").start();
	}
	
	
	private void write(String line) throws IOException {
		outputWriter.write(line + "\n");
		outputWriter.flush();
	}

	@Override
	protected OutputStream getCommandStream() {
		return commandStream;
	}

	@Override
	protected InputStream getErrorStream() {
		return errorStream;
	}

	@Override
	protected InputStream getOutputStream() {
		return outputStream;
	}


	protected abstract Move think(List<String> moveList, ThinkingParams thinkingParams);
}
