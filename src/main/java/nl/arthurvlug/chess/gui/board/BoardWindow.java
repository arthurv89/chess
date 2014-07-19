package nl.arthurvlug.chess.gui.board;

import java.awt.FontFormatException;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.game.GameStartedEvent;
import nl.arthurvlug.chess.events.BoardWindowInitializedEvent;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.Window;
import rx.Observable;
import rx.observers.EmptyObserver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Slf4j
@SuppressWarnings("serial")
@EventHandler
public class BoardWindow extends Window {
	@Inject private Game game;
	@Inject private EventBus eventBus;
	@Inject private ClockPane clockPane;

	@Override
	public void open() throws FontFormatException, IOException {
		setLayout(null);
		
		BoardPane boardPane = new BoardPane(game);
		boardPane.setBounds(50, 50, 420, 420);
		add(boardPane);
		
		clockPane.setBounds(500, 50, 200, 420);
		add(clockPane);

		EnginePane enginePane = new EnginePane();
		enginePane.setBounds(50, 500, 120, 100);
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
		final Observable<String> engineOutput = player.getEngineOutput();
		engineOutput.subscribe(new EmptyObserver<String>() {
			public void onNext(final String line) {
				if(line.contains(" score")) {
					log.info("    " + line);
				}
			}
		});
	}

	@Subscribe
	public void on(MoveAppliedEvent event) {
		repaint();
	}
}