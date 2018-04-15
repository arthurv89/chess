package nl.arthurvlug.chess.engine.ace.board;

import static nl.arthurvlug.chess.engine.EngineConstants.BLACK;
import static nl.arthurvlug.chess.engine.EngineConstants.WHITE;
import static nl.arthurvlug.chess.utils.board.FieldUtils.fieldIdx;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;

public class InitialACEBoard extends ACEBoard {
	private InitialACEBoard() {
		super();
		addPiece(WHITE, ROOK, fieldIdx("a1"));
		addPiece(WHITE, KNIGHT, fieldIdx("b1"));
		addPiece(WHITE, BISHOP, fieldIdx("c1"));
		addPiece(WHITE, QUEEN, fieldIdx("d1"));
		addPiece(WHITE, KING, fieldIdx("e1"));
		addPiece(WHITE, BISHOP, fieldIdx("f1"));
		addPiece(WHITE, KNIGHT, fieldIdx("g1"));
		addPiece(WHITE, ROOK, fieldIdx("h1"));

		addPiece(WHITE, PAWN, fieldIdx("a2"));
		addPiece(WHITE, PAWN, fieldIdx("b2"));
		addPiece(WHITE, PAWN, fieldIdx("c2"));
		addPiece(WHITE, PAWN, fieldIdx("d2"));
		addPiece(WHITE, PAWN, fieldIdx("e2"));
		addPiece(WHITE, PAWN, fieldIdx("f2"));
		addPiece(WHITE, PAWN, fieldIdx("g2"));
		addPiece(WHITE, PAWN, fieldIdx("h2"));

		addPiece(BLACK, ROOK, fieldIdx("a8"));
		addPiece(BLACK, KNIGHT, fieldIdx("b8"));
		addPiece(BLACK, BISHOP, fieldIdx("c8"));
		addPiece(BLACK, QUEEN, fieldIdx("d8"));
		addPiece(BLACK, KING, fieldIdx("e8"));
		addPiece(BLACK, BISHOP, fieldIdx("f8"));
		addPiece(BLACK, KNIGHT, fieldIdx("g8"));
		addPiece(BLACK, ROOK, fieldIdx("h8"));

		addPiece(BLACK, PAWN, fieldIdx("a7"));
		addPiece(BLACK, PAWN, fieldIdx("b7"));
		addPiece(BLACK, PAWN, fieldIdx("c7"));
		addPiece(BLACK, PAWN, fieldIdx("d7"));
		addPiece(BLACK, PAWN, fieldIdx("e7"));
		addPiece(BLACK, PAWN, fieldIdx("f7"));
		addPiece(BLACK, PAWN, fieldIdx("g7"));
		addPiece(BLACK, PAWN, fieldIdx("h7"));
	}

	public static InitialACEBoard createInitialACEBoard() {
		return new InitialACEBoard();
	}
}
