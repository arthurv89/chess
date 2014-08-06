package nl.arthurvlug.chess.domain.game;

import lombok.Getter;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;

import com.atlassian.fugue.Option;

public class GameFinished extends Move {
	@Getter
	private Move move;

	public GameFinished(Move move) {
		super(null, null, Option.<PieceType> none());
		this.move = move;
	}
}
