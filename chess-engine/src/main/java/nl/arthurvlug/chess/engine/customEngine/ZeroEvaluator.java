package nl.arthurvlug.chess.engine.customEngine;

public class ZeroEvaluator implements BoardEvaluator {
	private static final NormalScore evaluation = new NormalScore(0);
	
	@Override
	public Evaluation evaluate(AbstractEngineBoard movedBoard) {
		return evaluation;
	}
}
