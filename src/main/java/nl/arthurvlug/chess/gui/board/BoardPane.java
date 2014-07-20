package nl.arthurvlug.chess.gui.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JPanel;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.pieces.ColoredPiece;

import com.atlassian.fugue.Option;

@SuppressWarnings("serial")
public class BoardPane extends JPanel {
	private static final String FRITZ_FONT_FILENAME = "DiaTTFri.ttf";

	private static final int BOARD_OFFSET = 80;
	
	private final AbstractChessFont chessFont;
	private final Game game;
	
	public BoardPane(Game game) throws FontFormatException, IOException {
		this.game = game;
		this.chessFont = new TrueTypeFont(FRITZ_FONT_FILENAME);
		
		setLocation(0,  0);
		setLayout(null);
	}
	
	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		
		g.setBackground(Color.WHITE);
		drawBoard(g);
	}

	private void drawBoard(Graphics2D g) {
		g.drawString(game.getBlackPlayer().getName(), 0, 20);
		g.drawString(game.getWhitePlayer().getName(), 0, 520);
		
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
			String pieceString = chessFont.pieceString(coloredPieceOption);
			
			// Determine font and position of the string
			Font font = chessFont.deriveFont((float) fontSize());
			int stringWidth = g.getFontMetrics(font).stringWidth(pieceString);
			int xPos = (int) Math.round(x * fieldSize() + 0.5 * fieldSize() - 0.5 * stringWidth);
			int yPos = (7-y) * fieldSize() + fontSize() + BOARD_OFFSET;
			
			g.setBackground(Color.WHITE);
			g.setColor(Color.BLACK);
			g.setFont(font);
			g.drawString(pieceString, xPos, yPos);
		}
	}

	private void drawSquare(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.drawRect(x * fieldSize(), BOARD_OFFSET + (7-y) * fieldSize(), fieldSize(), fieldSize());
	}

	private int fieldSize() {
		return 50;
	}
	
	private int fontSize() {
		return (int) Math.round(0.8 * fieldSize());
	}
}
