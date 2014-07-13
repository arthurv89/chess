package nl.arthurvlug.chess.gui.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import nl.arthurvlug.chess.domain.game.BoardInitializedEvent;
import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.pieces.ColoredPiece;
import nl.arthurvlug.chess.events.BoardVisible;
import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.MoveAppliedEvent;
import nl.arthurvlug.chess.gui.Window;

import com.atlassian.fugue.Option;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SuppressWarnings("serial")
@EventHandler
public class BoardWindow extends Window {
	@Inject private Game game;
	@Inject private EventBus eventBus;

	@Override
	public void open() {
		add(new DrawPane());
		
		setSize(500, 500);
		setVisible(true);
		eventBus.post(new BoardVisible());
	}
	
	public class DrawPane extends JPanel {
		@Override
		public void paint(Graphics g1) {
			Graphics2D g = (Graphics2D) g1;
			
			drawBoard(g);
		}

		private void drawBoard(Graphics2D g) {
//			System.out.println(game.getBoard().toString());
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
			if(coloredPieceOption.isDefined()) {
				String pieceString = coloredPieceOption.get().getCharacterString();
				
				// Determine font and position of the string
				Font font = g.getFont().deriveFont((float) fontSize());
				int stringWidth = g.getFontMetrics(font).stringWidth(pieceString);
				int xPos = (int) Math.round(x * fieldSize() + 0.5 * fieldSize() - 0.5 * stringWidth);
				int yPos = (7-y) * fieldSize() + fontSize();
				
				g.setBackground(Color.WHITE);
				g.setColor(Color.BLACK);
				g.setFont(font);
				g.drawString(pieceString, xPos, yPos);
			}
		}

		private void drawSquare(Graphics2D g, int x, int y) {
			if((x+y) % 2 == 0) {
				g.setColor(Color.BLUE);
			} else {
				g.setColor(Color.YELLOW);
			}
			g.fillRect(x * fieldSize()+1, (7-y) * fieldSize()+1, fieldSize()-1, fieldSize()-1);

			g.setColor(Color.BLACK);
			g.drawRect(x * fieldSize(), (7-y) * fieldSize(), fieldSize(), fieldSize());
		}

		private int fieldSize() {
			return 50;
		}
		
		private int fontSize() {
			return (int) Math.round(0.8 * fieldSize());
		}
	}

	@Subscribe
	public void on(BoardInitializedEvent event) throws InterruptedException {
		open();
	}

	@Subscribe
	public void on(MoveAppliedEvent event) {
		repaint();
	}
}