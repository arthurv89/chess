package nl.arthurvlug.chess.engine.ace;

import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

public interface ChessEngineConfiguration {
	BoardEvaluator getEvaluator();

	int getQuiesceMaxDepth();

	int getSearchDepth();
}
