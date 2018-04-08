package nl.arthurvlug.chess.utils.board;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

@AllArgsConstructor
@Getter
@Value
public class Field {
	private Optional<ColoredPiece> piece;
}
