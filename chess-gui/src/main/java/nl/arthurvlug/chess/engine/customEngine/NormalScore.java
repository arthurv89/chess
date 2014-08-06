package nl.arthurvlug.chess.engine.customEngine;

import lombok.Getter;

public class NormalScore extends Evaluation {
	@Getter
	private Integer value;

	public NormalScore(Integer value) {
		this.value = value;
	}
}
