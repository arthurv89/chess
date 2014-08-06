package nl.arthurvlug.chess.gui.board;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JOptionPane;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.GameFinishedEvent;
import nl.arthurvlug.chess.events.GameStartedEvent;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.Window;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
@EventHandler
public class BoardWindow extends Window {
	static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 11);
	
	@Inject private Game game;
	@Inject private EventBus eventBus;
	@Inject private ClockPane clockPane;

	@Inject private MovesPane movesPane;

	@Override
	public void open() throws FontFormatException, IOException {
		setLayout(null);
		
		BoardPane boardPane = new BoardPane(game);
		boardPane.setBounds(50, 50, 420, 820);
		add(boardPane);
		
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
		open();

		listenToEngine(game.getWhitePlayer());
		listenToEngine(game.getBlackPlayer());
		eventBus.post(new BoardWindowInitializedEvent());
	}

	private void listenToEngine(ComputerPlayer player) {
//		final Observable<String> engineOutput = player.getEngineOutput();
//		engineOutput.subscribe(new MyEmptyObserver<String>() {
//			public void onNext(final String line) {
//			}
//		});
	}
	 
	@Subscribe
	public void on(MoveAppliedEvent event) {
		System.out.println(game.getBoard().toString() + "\n");
		repaint();
	}
	
	@Subscribe
	public void on(GameFinishedEvent event) {
		JOptionPane.showMessageDialog(this, "Game finished!");
	}
}