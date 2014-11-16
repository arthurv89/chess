package nl.arthurvlug.chess;

import java.io.Serializable;

public class MinPosition extends Position implements Serializable {
	private static final long serialVersionUID = 991638056051021055L;
	public MinPosition() {
		super(null, null);
		setScore(Integer.MIN_VALUE);
	}
}
