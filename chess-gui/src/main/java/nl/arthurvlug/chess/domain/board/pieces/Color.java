package nl.arthurvlug.chess.domain.board.pieces;

public enum Color {
	WHITE, BLACK;

	public Color other() {
		return this == WHITE ? BLACK : WHITE;
	}
}
