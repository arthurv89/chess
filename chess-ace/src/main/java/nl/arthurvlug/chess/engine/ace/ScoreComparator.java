package nl.arthurvlug.chess.engine.ace;

import java.util.Comparator;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;

public class ScoreComparator implements Comparator<ACEBoard> {
	@Override
	public int compare(ACEBoard o1, ACEBoard o2) {
		return o1.getSideBasedEvaluation() - o2.getSideBasedEvaluation();
	}
}
