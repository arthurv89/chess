package nl.arthurvlug.chess.engine.customEngine;

public interface ChessEngineConfiguration<T extends AbstractEngineBoard, R> {
	BoardEvaluator<T, R> getEvaluator();

	int getQuiesceMaxDepth();

	int getSearchDepth();
}
