package nl.arthurvlug.chess.engine.ace.configuration;

import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

public interface ChessEngineConfiguration {
	BoardEvaluator getEvaluator();

	int getQuiesceMaxDepth();

	int getSearchDepth();
}
