package nl.arthurvlug.chess.engine.customEngine;

import lombok.Value;
import nl.arthurvlug.chess.engine.ace.AceMove;

@Value
public class EvaluatedMove {
	private AceMove move;
	private NormalScore evaluation;
	
	public String toString() {
		return move.toString() + "=" + evaluation.getValue();
	}
}
