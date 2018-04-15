package nl.arthurvlug.chess.engine.customEngine;

public interface BoardEvaluator<T extends AbstractEngineBoard, R> {
	R evaluate(T board);
}
