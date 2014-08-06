package nl.arthurvlug.chess.engine.customEngine;

import java.awt.Color;

import lombok.Getter;

public class CheckmateScore extends Evaluation {
	@Getter
	private Color winner;

	public CheckmateScore(Color winner) {
		this.winner = winner;
	}
}
