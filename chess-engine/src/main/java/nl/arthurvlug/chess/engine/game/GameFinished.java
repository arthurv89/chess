package nl.arthurvlug.chess.engine.game;

import lombok.Getter;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import com.atlassian.fugue.Option;

public class GameFinished extends Move {
	@Getter
	private Move move;

	public GameFinished(Move move) {
		super(null, null, Option.<PieceType> none());
		this.move = move;
	}
}
