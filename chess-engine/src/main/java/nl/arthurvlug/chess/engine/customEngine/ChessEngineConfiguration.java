package nl.arthurvlug.chess.engine.customEngine;

public interface ChessEngineConfiguration<T extends AbstractEngineBoard<T>, R> {
	BoardEvaluator<T, R> getEvaluator();

	int getQuiesceMaxDepth();

	int getSearchDepth();
}
