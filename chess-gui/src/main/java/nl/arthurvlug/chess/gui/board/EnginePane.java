package nl.arthurvlug.chess.gui.board;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class EnginePane extends JPanel {
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		drawPane(g);
	}

	private void drawPane(Graphics2D g) {
		g.setFont(BoardWindow.DEFAULT_FONT);
		g.drawString("Blabla", 20, 20);
	}
}
