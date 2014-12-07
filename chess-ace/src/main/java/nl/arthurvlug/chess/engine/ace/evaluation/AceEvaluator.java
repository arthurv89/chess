package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;

import com.google.common.base.Function;

public class AceEvaluator implements BoardEvaluator {
	@Override
	public NormalScore evaluate(final AbstractEngineBoard board) {
		return (NormalScore) scoreFunction.apply((ACEBoard) board);
	}
	
	private final Function<ACEBoard, Evaluation> scoreFunction = (final ACEBoard engineBoard) -> {
		EvaluationHolder evaluationHolder = new EvaluationHolder();
		int score = evaluationHolder.calculate(engineBoard);
		
		return new NormalScore(score);
	};
}
