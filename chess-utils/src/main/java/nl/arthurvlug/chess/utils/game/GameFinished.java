package nl.arthurvlug.chess.utils.game;

import java.util.Optional;
import lombok.Getter;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class GameFinished extends Move {
	@Getter
	private Move move;

	public GameFinished(Move move) {
		super(null, null, Optional.empty());
		this.move = move;
	}

	@Override
	public String toString() {
		return "Game finished (" + move + ")";
	}
}
