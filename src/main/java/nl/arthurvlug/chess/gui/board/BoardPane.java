package nl.arthurvlug.chess.gui.board;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;

import nl.arthurvlug.chess.domain.game.Game;
import nl.arthurvlug.chess.domain.pieces.ColoredPiece;

import com.atlassian.fugue.Option;

@SuppressWarnings("serial")
public class BoardPane extends JPanel {
	private static final String CHESS_FONT_FILE_NAME = "chess.ttf";
	
	private final Font chessFont;
	private final Game game;

	public BoardPane(Game game) throws FontFormatException, IOException {
		this.game = game;
		this.chessFont = initializeFont();
		
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
			Font font = chessFont.deriveFont((float) fontSize());
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
		g.setColor(Color.BLACK);
		g.drawRect(x * fieldSize(), (7-y) * fieldSize(), fieldSize(), fieldSize());
	}

	private int fieldSize() {
		return 50;
	}
	
	private int fontSize() {
		return (int) Math.round(0.8 * fieldSize());
	}

	
	private Font initializeFont() throws FontFormatException, IOException {
		URL fontUrl = getClass().getResource("/" + CHESS_FONT_FILE_NAME);
		Font chessFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(chessFont);
		return chessFont;
	}
}
