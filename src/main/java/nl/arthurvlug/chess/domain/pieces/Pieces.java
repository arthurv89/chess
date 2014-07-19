package nl.arthurvlug.chess.domain.pieces;



public class Pieces {
	public static final Piece PAWN = new Pawn();
	public static final Piece KNIGHT = new Knight();
	public static final Piece BISHOP = new Bishop();
	public static final Piece ROOK = new Rook();
	public static final Piece QUEEN = new Queen();
	public static final Piece KING = new King();
	
	public static Piece fromChar(char character) {
		Piece[] x = new Piece[] {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING};
		for(Piece piece : x) {
			if(Character.toLowerCase(piece.getCharacter()) == Character.toLowerCase(character)) {
				return piece;
			}
		}
		throw new IllegalArgumentException("Could not find piece with char " + character);
	}
}
