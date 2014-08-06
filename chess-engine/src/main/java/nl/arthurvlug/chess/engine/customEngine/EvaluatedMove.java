package nl.arthurvlug.chess.engine.customEngine;

import lombok.Value;
import nl.arthurvlug.chess.utils.domain.game.Move;

@Value
public class EvaluatedMove {
	private Move move;
	private Evaluation evaluation;
}
