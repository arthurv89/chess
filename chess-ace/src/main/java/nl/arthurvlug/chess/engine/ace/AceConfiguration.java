package nl.arthurvlug.chess.engine.ace;

import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;

@Getter
@Setter
public class AceConfiguration implements ChessEngineConfiguration {
	private AceEvaluator evaluator = new AceEvaluator();
	private int searchDepth = 2;
	private int quiesceMaxDepth = 6;
}
