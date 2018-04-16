package nl.arthurvlug.chess.gui.components.board;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Optional;
import nl.arthurvlug.chess.gui.board.AbstractChessFont;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;

public class TrueTypeFont extends AbstractChessFont {
	public TrueTypeFont(String filename) throws FontFormatException, IOException {
		super(resolveFont(filename));
	}

	@Override
	public String pieceString(Optional<ColoredPiece> coloredPieceOption) {
		return PieceStringUtils.toCharacterString(coloredPieceOption.get(), PieceStringUtils.pieceToCharacterConverter);
	}
}
