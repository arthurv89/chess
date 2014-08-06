package nl.arthurvlug.chess.domain.board.pieces;

public enum Color {
	WHITE, BLACK;

	public boolean isWhite() {
		return WHITE == this;
	}

	public boolean isBlack() {
		return BLACK == this;
	}
	

	public Color other() {
		return this == WHITE ? BLACK : WHITE;
	}
}
