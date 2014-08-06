package nl.arthurvlug.chess.gui.board;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;

import lombok.AllArgsConstructor;
import nl.arthurvlug.chess.domain.board.pieces.ColoredPiece;

import com.atlassian.fugue.Option;

@AllArgsConstructor
public abstract class AbstractChessFont {
	private final Font font;
	
	public Font deriveFont(float fontSize) {
		return font.deriveFont(fontSize);
	}

	protected static Font resolveFont(String chessFontFileName) throws FontFormatException, IOException {
		URL fontUrl = AbstractChessFont.class.getResource("/fonts/" + chessFontFileName);
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(font);
		return font;
	}

	public abstract String pieceString(Option<ColoredPiece> coloredPieceOption);
}
