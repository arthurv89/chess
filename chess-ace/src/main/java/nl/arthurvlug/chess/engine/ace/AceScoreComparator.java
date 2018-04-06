package nl.arthurvlug.chess.engine.ace;

import java.util.Comparator;

import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;

public class AceScoreComparator implements Comparator<AbstractEngineBoard> {
	@Override
	public int compare(AbstractEngineBoard o1, AbstractEngineBoard o2) {
		return o1.getSideBasedEvaluation() - o2.getSideBasedEvaluation();
	}
}
