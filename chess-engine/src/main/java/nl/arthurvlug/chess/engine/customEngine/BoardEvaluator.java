package nl.arthurvlug.chess.engine.customEngine;

public interface BoardEvaluator {
	Evaluation evaluate(EngineBoard movedBoard);
}