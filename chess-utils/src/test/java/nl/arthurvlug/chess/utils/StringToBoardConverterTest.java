package nl.arthurvlug.chess.utils;

import nl.arthurvlug.chess.utils.board.Board;
import nl.arthurvlug.chess.utils.board.InitialBoard;
import nl.arthurvlug.chess.utils.board.InitializedBoard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringToBoardConverterTest {
	@Test
	public void testBoard() {
		final Board boardFromString = StringToBoardConverter.convert("" +
				"♖♘♗♕♔♗♘♖" +
				"♙♙♙♙♙♙♙♙" +
				"        " +
				"        " +
				"        " +
				"        " +
				"♟♟♟♟♟♟♟♟" +
				"♜♞♝♛♚♝♞♜");
		InitializedBoard initializedBoard = InitialBoard.create();
		assertEquals(boardFromString.toString(), initializedBoard.toString());
	}
}
