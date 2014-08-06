package nl.arthurvlug.chess.engine.customEngine;

import lombok.Getter;
import nl.arthurvlug.chess.domain.board.pieces.Color;

public class CheckmateScore extends Evaluation {
	@Getter
	private Color winner;

	public CheckmateScore(Color winner) {
		this.winner = winner;
	}
}
