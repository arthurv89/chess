package nl.arthurvlug.chess.engine.customEngine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThinkingParams {
	private int whiteTime = 1000000;
	private int blackTime = 1000000;
}
