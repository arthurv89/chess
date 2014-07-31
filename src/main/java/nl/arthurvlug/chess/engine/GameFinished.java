package nl.arthurvlug.chess.engine;

import lombok.Getter;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.Piece;

import com.atlassian.fugue.Option;

public class GameFinished extends Move {
	@Getter
	private Move move;

	public GameFinished(Move move) {
		super(null, null, Option.<Piece> none());
		this.move = move;
	}
}
