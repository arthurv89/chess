package nl.arthurvlug.chess.engine.ace;

import java.util.Comparator;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;

public class ScoreComparator implements Comparator<ACEBoard> {
	@Override
	public int compare(ACEBoard o1, ACEBoard o2) {
		int diff = o1.getEvaluation() - o2.getEvaluation();
		if(diff == 0) {
			return o1.toString().compareTo(o2.toString());
		}
		return diff;
	}
}
