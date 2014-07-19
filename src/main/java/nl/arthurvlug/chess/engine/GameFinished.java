package nl.arthurvlug.chess.engine;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.Piece;

import com.atlassian.fugue.Option;

public class GameFinished extends Move {
	public GameFinished() {
		super(null, null, Option.<Piece> none());
	}
}
