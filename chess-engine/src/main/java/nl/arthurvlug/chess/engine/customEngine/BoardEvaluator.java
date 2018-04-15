package nl.arthurvlug.chess.engine.customEngine;

public interface BoardEvaluator<T extends AbstractEngineBoard<T>, R> {
	R evaluate(T board);
}
