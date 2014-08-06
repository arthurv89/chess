package nl.arthurvlug.chess.gui.components.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import nl.arthurvlug.chess.gui.domain.game.Game;
import nl.arthurvlug.chess.utils.domain.board.pieces.ColoredPiece;

import com.atlassian.fugue.Option;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class DrawPane extends JPanel {
	@Inject private Game game;
	
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;

		drawBoard(g);
	}

	private void drawBoard(Graphics2D g) {
		g.setColor(Color.RED);

		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				drawSquare(g, x, y);
				drawPiece(g, x, y);
			}
		}
	}

	private void drawPiece(Graphics2D g, int x, int y) {
		Option<ColoredPiece> coloredPieceOption = game.getBoard().getPiece(x, y);
		if (coloredPieceOption.isDefined()) {
			String pieceString = coloredPieceOption.get().getCharacterString();

			// Determine font and position of the string
			Font font = g.getFont().deriveFont((float) fontSize());
			int stringWidth = g.getFontMetrics(font).stringWidth(pieceString);
			int xPos = (int) Math.round(x * fieldSize() + 0.5 * fieldSize() - 0.5 * stringWidth);
			int yPos = (7 - y) * fieldSize() + fontSize();

			g.setBackground(Color.WHITE);
			g.setColor(Color.BLACK);
			g.setFont(font);
			g.drawString(pieceString, xPos, yPos);
		}
	}

	private void drawSquare(Graphics2D g, int x, int y) {
		if ((x + y) % 2 == 0) {
			g.setColor(Color.BLUE);
		} else {
			g.setColor(Color.YELLOW);
		}
		g.fillRect(x * fieldSize() + 1, (7 - y) * fieldSize() + 1, fieldSize() - 1, fieldSize() - 1);

		g.setColor(Color.BLACK);
		g.drawRect(x * fieldSize(), (7 - y) * fieldSize(), fieldSize(), fieldSize());
	}

	private int fieldSize() {
		return 50;
	}

	private int fontSize() {
		return (int) Math.round(0.8 * fieldSize());
	}
}
