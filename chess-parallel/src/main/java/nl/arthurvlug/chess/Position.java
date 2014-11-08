package nl.arthurvlug.chess;

import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;

public class Position implements Comparable<Position> {
	@Getter
	private Position parentPosition;
	@Getter
	private String lastMove;
	@Getter
	@Setter
	private int score = 0;
	@Getter
	private ACEBoard newBoard;
	
//	public String getAncestorsAndCurrent() {
//		return parent + " " + move;
//	}
//	
	public Position() { }

	public Position(String move, Position parent, ACEBoard newBoard) {
		this.lastMove = move;
		this.parentPosition = parent;
		this.newBoard = newBoard;
	}
	
	public static Position ROOT_MIN = new Position("",  null, null) {{
		setScore(Integer.MIN_VALUE);
	}};
	
	public static Position ROOT_MAX = new Position("",  null, null) {{
		setScore(Integer.MAX_VALUE);
	}};
	
	String getCurrentAndAncestors() {
		String s = "";
		s += (parentPosition != null) ? parentPosition.getCurrentAndAncestors() + "" : "";
		s += lastMove;
		return s;
	}
	
	@Override
	public String toString() {
		return getCurrentAndAncestors() + " (v=" + score + ")";
	}

	@Override
	public int compareTo(Position o) {
		return score - o.score;
	}
	
	@Override
	public boolean equals(Object obj) {
		Position other = (Position) obj;
		return getCurrentAndAncestors().equals(other.getCurrentAndAncestors());
	}
}
