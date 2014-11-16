package nl.arthurvlug.chess;

class MaxPosition extends Position {
	private static final long serialVersionUID = 2678478297413714506L;
	MaxPosition() {
		super(null, null);
		setScore(Integer.MAX_VALUE);
	}
}