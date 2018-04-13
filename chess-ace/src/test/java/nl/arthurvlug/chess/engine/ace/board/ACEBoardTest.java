package nl.arthurvlug.chess.engine.ace.board;

import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.utils.ACEBoardUtils;
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
		
		long expectedWhiteEnemyAndEmptyBoard = ~(
				bitboardFromFieldName("a1") |
				bitboardFromFieldName("b1") |
				bitboardFromFieldName("c1") |
				bitboardFromFieldName("d1") |
				bitboardFromFieldName("e1") |
				bitboardFromFieldName("f1") |
				bitboardFromFieldName("f7")
		);
		assertEquals(expectedWhiteEnemyAndEmptyBoard, startPositionBoard.enemy_and_empty_board);
		
		

		
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

		copyBoard = new ACEBoard(copyBoard, EngineConstants.BLACK, false);
		long expectedBlackEnemyAndEmptyBoard = ~(
				bitboardFromFieldName("a7") |
				bitboardFromFieldName("b7") |
				bitboardFromFieldName("c7") |
				bitboardFromFieldName("d7") |
				bitboardFromFieldName("e7") |
				bitboardFromFieldName("f7")
		); // TODO: Use bitboardFromBoard(...)
		assertEquals(expectedBlackEnemyAndEmptyBoard, copyBoard.enemy_and_empty_board);
		
		
		
		assertEquals(startPositionBoard.white_pawns, bitboardFromFieldName("a1") | bitboardFromFieldName("f7"));
		assertEquals(startPositionBoard.white_knights, bitboardFromFieldName("b1"));
		assertEquals(startPositionBoard.white_bishops, bitboardFromFieldName("c1"));
		assertEquals(startPositionBoard.white_rooks, bitboardFromFieldName("d1"));
		assertEquals(startPositionBoard.white_queens, bitboardFromFieldName("e1"));
		assertEquals(startPositionBoard.white_kings, bitboardFromFieldName("f1"));
		
		assertEquals(copyBoard.white_pawns, bitboardFromFieldName("a2"));
		assertEquals(copyBoard.white_knights, bitboardFromFieldName("b2"));
		assertEquals(copyBoard.white_bishops, bitboardFromFieldName("c2"));
		assertEquals(copyBoard.white_rooks, bitboardFromFieldName("d2"));
		assertEquals(copyBoard.white_queens, bitboardFromFieldName("e2"));
		assertEquals(copyBoard.white_kings, bitboardFromFieldName("f2"));
		
		assertEquals(startPositionBoard.black_pawns, bitboardFromFieldName("a8"));
		assertEquals(startPositionBoard.black_knights, bitboardFromFieldName("b8"));
		assertEquals(startPositionBoard.black_bishops, bitboardFromFieldName("c8"));
		assertEquals(startPositionBoard.black_rooks, bitboardFromFieldName("d8"));
		assertEquals(startPositionBoard.black_queens, bitboardFromFieldName("e8"));
		assertEquals(startPositionBoard.black_kings, bitboardFromFieldName("f8"));
		
		assertEquals(copyBoard.black_pawns, bitboardFromFieldName("a7"));
		assertEquals(copyBoard.black_knights, bitboardFromFieldName("b7"));
		assertEquals(copyBoard.black_bishops, bitboardFromFieldName("c7"));
		assertEquals(copyBoard.black_rooks, bitboardFromFieldName("d7"));
		assertEquals(copyBoard.black_queens, bitboardFromFieldName("e7"));
		assertEquals(copyBoard.black_kings, bitboardFromFieldName("f7"));

		assertEquals(startPositionBoard.whiteOccupiedSquares,
				bitboardFromFieldName("a1") |
				bitboardFromFieldName("b1") |
				bitboardFromFieldName("c1") |
				bitboardFromFieldName("d1") |
				bitboardFromFieldName("e1") |
				bitboardFromFieldName("f1") |
				bitboardFromFieldName("f7"));
		assertEquals(startPositionBoard.blackOccupiedSquares,
				bitboardFromFieldName("a8") |
				bitboardFromFieldName("b8") |
				bitboardFromFieldName("c8") |
				bitboardFromFieldName("d8") |
				bitboardFromFieldName("e8") |
				bitboardFromFieldName("f8"));

		assertEquals(copyBoard.whiteOccupiedSquares, 
				bitboardFromFieldName("a2") |
				bitboardFromFieldName("b2") |
				bitboardFromFieldName("c2") |
				bitboardFromFieldName("d2") |
				bitboardFromFieldName("e2") |
				bitboardFromFieldName("f2"));
		assertEquals(copyBoard.blackOccupiedSquares, 
				bitboardFromFieldName("a7") |
				bitboardFromFieldName("b7") |
				bitboardFromFieldName("c7") |
				bitboardFromFieldName("d7") |
				bitboardFromFieldName("e7") |
				bitboardFromFieldName("f7"));
		
		assertTrue(copyBoard.lastMoveWasTakeMove);
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
