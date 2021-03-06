package nl.arthurvlug.chess.utils.board;

import com.google.common.collect.ImmutableMap;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

import static nl.arthurvlug.chess.utils.board.InitializedBoard.c;
import static nl.arthurvlug.chess.utils.board.InitializedBoard.p;
import static nl.arthurvlug.chess.utils.board.pieces.Color.BLACK;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;

public class InitialBoard {
	private InitialBoard() {
		throw new IllegalStateException("Please use static methods");
	}

	private static ImmutableMap<Coordinates, ColoredPiece> piecesMap() {
		return ImmutableMap.<Coordinates, ColoredPiece> builder()
				.put(c(0, 0), p(ROOK, WHITE))
				.put(c(1, 0), p(KNIGHT, WHITE))
				.put(c(2, 0), p(BISHOP, WHITE))
				.put(c(3, 0), p(QUEEN, WHITE))
				.put(c(4, 0), p(KING, WHITE))
				.put(c(5, 0), p(BISHOP, WHITE))
				.put(c(6, 0), p(KNIGHT, WHITE))
				.put(c(7, 0), p(ROOK, WHITE))
				.put(c(0, 1), p(PAWN, WHITE))
				.put(c(1, 1), p(PAWN, WHITE))
				.put(c(2, 1), p(PAWN, WHITE))
				.put(c(3, 1), p(PAWN, WHITE))
				.put(c(4, 1), p(PAWN, WHITE))
				.put(c(5, 1), p(PAWN, WHITE))
				.put(c(6, 1), p(PAWN, WHITE))
				.put(c(7, 1), p(PAWN, WHITE))

				.put(c(0, 6), p(PAWN, BLACK))
				.put(c(1, 6), p(PAWN, BLACK))
				.put(c(2, 6), p(PAWN, BLACK))
				.put(c(3, 6), p(PAWN, BLACK))
				.put(c(4, 6), p(PAWN, BLACK))
				.put(c(5, 6), p(PAWN, BLACK))
				.put(c(6, 6), p(PAWN, BLACK))
				.put(c(7, 6), p(PAWN, BLACK))
				.put(c(0, 7), p(ROOK, BLACK))
				.put(c(1, 7), p(KNIGHT, BLACK))
				.put(c(2, 7), p(BISHOP, BLACK))
				.put(c(3, 7), p(QUEEN, BLACK))
				.put(c(4, 7), p(KING, BLACK))
				.put(c(5, 7), p(BISHOP, BLACK))
				.put(c(6, 7), p(KNIGHT, BLACK))
				.put(c(7, 7), p(ROOK, BLACK))
				.build();
	}

	public static InitializedBoard create() {
		return new InitializedBoard(piecesMap());
	}
}
