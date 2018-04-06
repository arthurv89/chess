package nl.arthurvlug.chess.engine.customEngine;

import lombok.Value;
import nl.arthurvlug.chess.utils.game.Move;

@Value
public class EvaluatedMove {
	private Move move;
	private NormalScore evaluation;
	
	public String toString() {
		return move.toString() + "=" + evaluation.getValue();
	}
}
