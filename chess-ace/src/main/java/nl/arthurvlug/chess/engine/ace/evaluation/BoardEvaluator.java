package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;

public abstract class BoardEvaluator {
	public abstract Integer evaluate(ACEBoard aceBoard);
}
