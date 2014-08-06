package nl.arthurvlug.chess.engine.customEngine;

import nl.arthurvlug.chess.domain.board.Board;

public interface BoardEvaluator {
	Evaluation evaluate(Board board);
}
