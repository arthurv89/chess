package nl.arthurvlug.chess.domain.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.pieces.Piece;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
@ToString
public class Move {
	private Coordinates from;
	private Coordinates to;
	private Option<Piece> promotionPiece;
}
