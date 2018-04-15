package nl.arthurvlug.chess.engine.ace.board;

import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromBoard;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.utils.ACEBoardUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Test;

public class ACEBoardTest {
	@Test
	public void testBitboardMoveAll() throws Exception {
		final ACEBoard startPositionBoard = ACEBoardUtils.initializedBoard(WHITE, "" +
				"♟♞♝♜♛♚..\n" +
				".....♙..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♙♘♗♖♕♔..\n");

		verifyStartPositionBoard(startPositionBoard);


		ACEBoard copyBoard = playMoves(startPositionBoard);

		copyBoard = new ACEBoard(copyBoard, EngineConstants.BLACK, false);
		verifyCopyBoard(copyBoard);
	}

	private ACEBoard playMoves(final ACEBoard startPositionBoard) {
		ACEBoard copyBoard = startPositionBoard;
		copyBoard = apply("a1", "a2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("a8", "a7", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("b1", "b2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("b8", "b7", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("c1", "c2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("c8", "c7", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("d1", "d2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("d8", "d7", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("e1", "e2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("e8", "e7", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("f1", "f2", copyBoard);
		assertFalse(copyBoard.lastMoveWasTakeMove);

		copyBoard = apply("f8", "f7", copyBoard);
		assertTrue(copyBoard.lastMoveWasTakeMove);
		return copyBoard;
	}

	private void verifyStartPositionBoard(final ACEBoard startPositionBoard) {
		assertEquals(startPositionBoard.white_pawns, bitboardFromFieldName("a1 f7"));
		assertEquals(startPositionBoard.white_knights, bitboardFromFieldName("b1"));
		assertEquals(startPositionBoard.white_bishops, bitboardFromFieldName("c1"));
		assertEquals(startPositionBoard.white_rooks, bitboardFromFieldName("d1"));
		assertEquals(startPositionBoard.white_queens, bitboardFromFieldName("e1"));
		assertEquals(startPositionBoard.white_kings, bitboardFromFieldName("f1"));

		assertEquals(startPositionBoard.black_pawns, bitboardFromFieldName("a8"));
		assertEquals(startPositionBoard.black_knights, bitboardFromFieldName("b8"));
		assertEquals(startPositionBoard.black_bishops, bitboardFromFieldName("c8"));
		assertEquals(startPositionBoard.black_rooks, bitboardFromFieldName("d8"));
		assertEquals(startPositionBoard.black_queens, bitboardFromFieldName("e8"));
		assertEquals(startPositionBoard.black_kings, bitboardFromFieldName("f8"));

		assertEquals(startPositionBoard.enemy_and_empty_board, bitboardFromBoard(
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟.♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"......♟♟\n"));

		assertEquals(startPositionBoard.whiteOccupiedSquares, bitboardFromBoard(
				"........\n" +
				".....♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♟♟♟♟♟♟..\n"));

		assertEquals(startPositionBoard.blackOccupiedSquares, bitboardFromBoard(
				"♟♟♟♟♟♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n"));
	}

	private void verifyCopyBoard(final ACEBoard copyBoard) {
		assertEquals(copyBoard.white_pawns, bitboardFromFieldName("a2"));
		assertEquals(copyBoard.white_knights, bitboardFromFieldName("b2"));
		assertEquals(copyBoard.white_bishops, bitboardFromFieldName("c2"));
		assertEquals(copyBoard.white_rooks, bitboardFromFieldName("d2"));
		assertEquals(copyBoard.white_queens, bitboardFromFieldName("e2"));
		assertEquals(copyBoard.white_kings, bitboardFromFieldName("f2"));

		assertEquals(copyBoard.black_pawns, bitboardFromFieldName("a7"));
		assertEquals(copyBoard.black_knights, bitboardFromFieldName("b7"));
		assertEquals(copyBoard.black_bishops, bitboardFromFieldName("c7"));
		assertEquals(copyBoard.black_rooks, bitboardFromFieldName("d7"));
		assertEquals(copyBoard.black_queens, bitboardFromFieldName("e7"));
		assertEquals(copyBoard.black_kings, bitboardFromFieldName("f7"));

		assertEquals(copyBoard.enemy_and_empty_board, bitboardFromBoard(
				"♟♟♟♟♟♟♟♟\n" +
				"......♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n"));

		assertEquals(copyBoard.whiteOccupiedSquares, bitboardFromBoard(
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♟♟♟♟♟♟..\n" +
				"........\n"));

		assertEquals(copyBoard.blackOccupiedSquares, bitboardFromBoard(
				"........\n" +
				"♟♟♟♟♟♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n"));

		assertTrue(copyBoard.lastMoveWasTakeMove);
	}

	@Test
	public void testAceBoard() {
		final ACEBoard oldBoard = new InitialACEBoard();
		oldBoard.finalizeBitboards();

		final ACEBoard newBoard = oldBoard.clone();
		Move move = MoveUtils.toMove("e2e4");
		newBoard.apply(move);
		newBoard.unapply(move);
		assertEquals(ACEBoardUtils.dump(oldBoard), ACEBoardUtils.dump(newBoard));
	}

	private ACEBoard apply(String from, String to, ACEBoard board) {
		Move move = new Move(
				FieldUtils.coordinates(from),
				FieldUtils.coordinates(to),
				Optional.empty());
		ACEBoard copiedBoard = new ACEBoard(board);
		copiedBoard.finalizeBitboards();
		copiedBoard.apply(move);
		return copiedBoard;
	}
}
