package nl.arthurvlug.chess.engine.ace;

import lombok.Getter;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;

@Getter
public class SimpleChessEngineConfiguration implements ChessEngineConfiguration {
	private final SimplePieceEvaluator evaluator = new SimplePieceEvaluator();
	private final int searchDepth = 4;
	private final int quiesceMaxDepth = 4;
}
