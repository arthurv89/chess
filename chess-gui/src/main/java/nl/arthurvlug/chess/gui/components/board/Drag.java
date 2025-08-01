package nl.arthurvlug.chess.gui.components.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.board.Coordinates;

@AllArgsConstructor
@Getter
public class Drag {
	private final Coordinates currentMouseLocation;
	private final char pieceChar;
	private final Coordinates beginField;
	private final Coordinates currentField;
}
