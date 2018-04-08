package nl.arthurvlug.chess.engine.ace.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.utils.game.Move;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import org.junit.Test;

public class ACEBoardTest {
	@Test
	public void testBitboardMoveAll() throws Exception {
		ACEBoard startPositionBoard = new ACEBoard(EngineConstants.WHITE, false);
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("a1"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("b1"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("c1"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("d1"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, BitboardUtils.toIndex("e1"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("f1"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, BitboardUtils.toIndex("a8"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("b8"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("c8"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("d8"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, BitboardUtils.toIndex("e8"));
		startPositionBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("f8"));
		startPositionBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("f7")); // Black king takes this pawn
		startPositionBoard.finalizeBitboards();
		
		long expectedWhiteEnemyAndEmptyBoard = ~(
				BitboardUtils.bitboardFromString("a1") |
				BitboardUtils.bitboardFromString("b1") |
				BitboardUtils.bitboardFromString("c1") |
				BitboardUtils.bitboardFromString("d1") |
				BitboardUtils.bitboardFromString("e1") |
				BitboardUtils.bitboardFromString("f1") |
				BitboardUtils.bitboardFromString("f7")
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
				BitboardUtils.bitboardFromString("a7") |
				BitboardUtils.bitboardFromString("b7") |
				BitboardUtils.bitboardFromString("c7") |
				BitboardUtils.bitboardFromString("d7") |
				BitboardUtils.bitboardFromString("e7") |
				BitboardUtils.bitboardFromString("f7")
		);
		assertEquals(expectedBlackEnemyAndEmptyBoard, copyBoard.enemy_and_empty_board);
		
		
		
		assertEquals(startPositionBoard.white_pawns, BitboardUtils.bitboardFromString("a1") | BitboardUtils.bitboardFromString("f7"));
		assertEquals(startPositionBoard.white_knights, BitboardUtils.bitboardFromString("b1"));
		assertEquals(startPositionBoard.white_bishops, BitboardUtils.bitboardFromString("c1"));
		assertEquals(startPositionBoard.white_rooks, BitboardUtils.bitboardFromString("d1"));
		assertEquals(startPositionBoard.white_queens, BitboardUtils.bitboardFromString("e1"));
		assertEquals(startPositionBoard.white_kings, BitboardUtils.bitboardFromString("f1"));
		
		assertEquals(copyBoard.white_pawns, BitboardUtils.bitboardFromString("a2"));
		assertEquals(copyBoard.white_knights, BitboardUtils.bitboardFromString("b2"));
		assertEquals(copyBoard.white_bishops, BitboardUtils.bitboardFromString("c2"));
		assertEquals(copyBoard.white_rooks, BitboardUtils.bitboardFromString("d2"));
		assertEquals(copyBoard.white_queens, BitboardUtils.bitboardFromString("e2"));
		assertEquals(copyBoard.white_kings, BitboardUtils.bitboardFromString("f2"));
		
		assertEquals(startPositionBoard.black_pawns, BitboardUtils.bitboardFromString("a8"));
		assertEquals(startPositionBoard.black_knights, BitboardUtils.bitboardFromString("b8"));
		assertEquals(startPositionBoard.black_bishops, BitboardUtils.bitboardFromString("c8"));
		assertEquals(startPositionBoard.black_rooks, BitboardUtils.bitboardFromString("d8"));
		assertEquals(startPositionBoard.black_queens, BitboardUtils.bitboardFromString("e8"));
		assertEquals(startPositionBoard.black_kings, BitboardUtils.bitboardFromString("f8"));
		
		assertEquals(copyBoard.black_pawns, BitboardUtils.bitboardFromString("a7"));
		assertEquals(copyBoard.black_knights, BitboardUtils.bitboardFromString("b7"));
		assertEquals(copyBoard.black_bishops, BitboardUtils.bitboardFromString("c7"));
		assertEquals(copyBoard.black_rooks, BitboardUtils.bitboardFromString("d7"));
		assertEquals(copyBoard.black_queens, BitboardUtils.bitboardFromString("e7"));
		assertEquals(copyBoard.black_kings, BitboardUtils.bitboardFromString("f7"));

		assertEquals(startPositionBoard.whiteOccupiedSquares,
				BitboardUtils.bitboardFromString("a1") |
				BitboardUtils.bitboardFromString("b1") |
				BitboardUtils.bitboardFromString("c1") |
				BitboardUtils.bitboardFromString("d1") |
				BitboardUtils.bitboardFromString("e1") |
				BitboardUtils.bitboardFromString("f1") |
				BitboardUtils.bitboardFromString("f7"));
		assertEquals(startPositionBoard.blackOccupiedSquares,
				BitboardUtils.bitboardFromString("a8") |
				BitboardUtils.bitboardFromString("b8") |
				BitboardUtils.bitboardFromString("c8") |
				BitboardUtils.bitboardFromString("d8") |
				BitboardUtils.bitboardFromString("e8") |
				BitboardUtils.bitboardFromString("f8"));

		assertEquals(copyBoard.whiteOccupiedSquares, 
				BitboardUtils.bitboardFromString("a2") |
				BitboardUtils.bitboardFromString("b2") |
				BitboardUtils.bitboardFromString("c2") |
				BitboardUtils.bitboardFromString("d2") |
				BitboardUtils.bitboardFromString("e2") |
				BitboardUtils.bitboardFromString("f2"));
		assertEquals(copyBoard.blackOccupiedSquares, 
				BitboardUtils.bitboardFromString("a7") |
				BitboardUtils.bitboardFromString("b7") |
				BitboardUtils.bitboardFromString("c7") |
				BitboardUtils.bitboardFromString("d7") |
				BitboardUtils.bitboardFromString("e7") |
				BitboardUtils.bitboardFromString("f7"));
		
		assertTrue(copyBoard.lastMoveWasTakeMove);
	}

	private ACEBoard apply(String from, String to, ACEBoard board) {
		Move move = new Move(
				BitboardUtils.coordinates(from), 
				BitboardUtils.coordinates(to),
				Optional.empty());
		ACEBoard copiedBoard = new ACEBoard(board);
		copiedBoard.finalizeBitboards();
		copiedBoard.apply(move);
		return copiedBoard;
	}
}
