package nl.arthurvlug.chess;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class Position implements Comparable<Position>, Serializable {
	private static final long serialVersionUID = -1030887784637657981L;

	public static final Position MIN_POSITION = new Position(null, null, Integer.MIN_VALUE);
	protected static final Position MAX_POSITION = new Position(null, null, Integer.MAX_VALUE);
		
	@Getter
	private Position parentPosition;
	@Getter
	private final String lastMove;
	@Getter
	@Setter @Accessors(chain = true)
	private int score = 0;
	
	public Position() {
		lastMove = null;
	}

	public Position(String move, Position parent) {
		this.lastMove = move;
		this.parentPosition = parent;
	}
	
	private Position(String move, Position parent, int score) {
		this(move, parent);
		setScore(score);
	}

	public Position(Position childPosition) {
		this.lastMove = childPosition.lastMove;
		this.parentPosition = childPosition.parentPosition;
		this.score = childPosition.score;
	}

	String getCurrentAndAncestorsString() {
		String s = "";
		if(parentPosition != null) {
			s += parentPosition.getCurrentAndAncestorsString();
			s += lastMove;
		}
		return s;
	}

	public List<String> getCurrentAndAncestorMoves() {
		Position position = this;
		
		LinkedList<String> list = new LinkedList<String>();
		while(position.getParentPosition() != null) {
			list.addFirst(position.getLastMove());
			position = position.parentPosition;
		}
		return list;
	}
	
	@Override
	public String toString() {
		NumberFormat nf = new DecimalFormat("0.00");
		return getCurrentAndAncestorsString() + " (v=" + nf.format(score*0.01) + ")";
	}

	@Override
	public int compareTo(Position o) {
		return score - o.score;
	}
	
	@Override
	public boolean equals(Object obj) {
		Position other = (Position) obj;
		return getCurrentAndAncestorsString().equals(other.getCurrentAndAncestorsString());
	}
}
