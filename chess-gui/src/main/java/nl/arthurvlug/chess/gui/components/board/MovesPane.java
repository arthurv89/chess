package nl.arthurvlug.chess.gui.components.board;

import com.google.common.eventbus.Subscribe;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import nl.arthurvlug.chess.gui.events.EventHandler;
import nl.arthurvlug.chess.gui.events.GameStartedEvent;
import nl.arthurvlug.chess.gui.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.game.Game;
import nl.arthurvlug.chess.utils.game.Move;

@SuppressWarnings("serial")
@EventHandler
public class MovesPane extends JPanel {
	private Game game;
	
	
	private JTextArea textArea = new JTextArea(6, 20);
	
	public MovesPane() {
		textArea.setSize(300, 200);
		add(textArea);
	}
	
	@Override
	public void paint(Graphics g) {
		if(game != null) {
			textArea.setText("");
			textArea.setLineWrap(true);
			for(Move move : game.immutableMoveList()) {
				textArea.append(move.toString() + " ");
			}
		}
	}
	
	@Subscribe
	public void on(MoveAppliedEvent event) {
		repaint();
	}
	
	@Subscribe
	public void on(GameStartedEvent event) {
		this.game = event.getGame();
	}
}
