package nl.arthurvlug.chess.utils.domain.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.utils.domain.board.pieces.ColoredPiece;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
@Value
public class Field {
	private Option<ColoredPiece> piece;
}
