package nl.arthurvlug.chess.gui.components.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import nl.arthurvlug.chess.gui.components.Window;
import nl.arthurvlug.chess.gui.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.utils.EventHandler;
import nl.arthurvlug.chess.gui.events.GameFinishedEvent;
import nl.arthurvlug.chess.gui.events.GameStartedEvent;
import nl.arthurvlug.chess.gui.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.game.Game;
import nl.arthurvlug.chess.utils.ThinkEvent;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

@SuppressWarnings("serial")
@EventHandler
public class BoardWindow extends Window {
	private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
	private static final String PIECE_MOVED_MP3 = "./piece_moved.mp3";

	static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 11);
	
	private Game game;
	@Inject private EventBus eventBus;
	@Inject private ClockPane clockPane;

	@Inject private MovesPane movesPane;
	private BoardPanel boardPanel;

	@Subscribe
	public void on(ThinkEvent event) {
		boardPanel.nodesEvaluated = event.getNodesEvaluated();
		boardPanel.repaint();
	}

	@Override
	public void open() throws FontFormatException, IOException {
		setLayout(null);
		
		boardPanel = new BoardPanel(game);
		boardPanel.setBounds(50, 50, 420, 820);
		add(boardPanel);
		
		clockPane.setBounds(550, 50, 200, 420);
		add(clockPane);
		
		movesPane.setBounds(570, 100, 200, 420);
		add(movesPane);

		EnginePane enginePane = new EnginePane();
		enginePane.setBounds(50, 600, 120, 100);
		add(enginePane);
		
		setSize(800, 800);
		setLocation(500, 50);
		setVisible(true);
	}

	@Subscribe
	public void on(GameStartedEvent event) throws InterruptedException, FontFormatException, IOException {
		this.game = event.getGame();
		
		open();

		eventBus.post(new BoardWindowInitializedEvent());
	}

	@Subscribe
	public void on(MoveAppliedEvent event) {
//		System.out.println(game.getBoard().toString() + "\n");

		playSound(PIECE_MOVED_MP3);

		boardPanel.setBoard(game.getBoard(), event.getMove());
		repaint();
	}

	private void playSound(final String url) {
		new Thread(() -> {
			try {
				FileInputStream in = new FileInputStream(new File(url).toURI().toString());
				AudioStream as = new AudioStream(in);
				AudioPlayer.player.start(as);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	@Subscribe
	public void on(GameFinishedEvent event) {
		JOptionPane.showMessageDialog(this, "Game finished!");
	}
}