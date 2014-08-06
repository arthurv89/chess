package nl.arthurvlug.chess.engine.customEngine;

import lombok.Data;

@Data
public class ThinkingParams {
	private int whiteTime = 1000;
	private int blackTime = 1000;
}
