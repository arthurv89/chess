package nl.arthurvlug.chess.engine.ace.board;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.EngineUtils;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import org.junit.Test;

import com.atlassian.fugue.Option;

public class ACEBoardTest {
	@Test
	public void testBitboardMoveAll() throws Exception {
		ACEBoard startPositionBoard = new ACEBoard(EngineConstants.WHITE);
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
		copyBoard = apply(PieceType.PAWN, "a1", "a2", copyBoard, EngineConstants.WHITE);
		
		assertFalse(copyBoard.lastMoveWasTakeMove);
		
		copyBoard = apply(PieceType.PAWN, "a8", "a7", copyBoard, EngineConstants.BLACK);
		copyBoard = apply(PieceType.KNIGHT, "b1", "b2", copyBoard, EngineConstants.WHITE);
		copyBoard = apply(PieceType.KNIGHT, "b8", "b7", copyBoard, EngineConstants.BLACK);
		copyBoard = apply(PieceType.BISHOP, "c1", "c2", copyBoard, EngineConstants.WHITE);
		copyBoard = apply(PieceType.BISHOP, "c8", "c7", copyBoard, EngineConstants.BLACK);

		
		copyBoard = apply(PieceType.ROOK, "d1", "d2", copyBoard, EngineConstants.WHITE);
		copyBoard = apply(PieceType.ROOK, "d8", "d7", copyBoard, EngineConstants.BLACK);
		copyBoard = apply(PieceType.QUEEN, "e1", "e2", copyBoard, EngineConstants.WHITE);
		copyBoard = apply(PieceType.QUEEN, "e8", "e7", copyBoard, EngineConstants.BLACK);
		copyBoard = apply(PieceType.KING, "f1", "f2", copyBoard, EngineConstants.WHITE);
		copyBoard = apply(PieceType.KING, "f8", "f7", copyBoard, EngineConstants.BLACK);
		
		assertTrue(copyBoard.lastMoveWasTakeMove);

		copyBoard = new ACEBoard(copyBoard, EngineConstants.BLACK);
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

	@Test
	public void testBitboardNotCheck() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("a3"));
		engineBoard.finalizeBitboards();
		engineBoard.generateSuccessorBoards(MoveGenerator.generateMoves(engineBoard));

		assertFalse(engineBoard.currentPlayerInCheck);
	}

	@Test
	public void testBitboardWhiteCheck() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("a2"));
		engineBoard.finalizeBitboards();
		engineBoard.generateSuccessorBoards(MoveGenerator.generateMoves(engineBoard));

		assertTrue(engineBoard.currentPlayerInCheck);
	}

	@Test
	public void testBitboardBlackCheck() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.BLACK);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("a2"));
		engineBoard.finalizeBitboards();
		engineBoard.generateSuccessorBoards(MoveGenerator.generateMoves(engineBoard));

		assertTrue(engineBoard.currentPlayerInCheck);
	}

	private ACEBoard apply(PieceType pieceType, String from, String to, ACEBoard board, int color) {
		AceMove move = new AceMove(pieceType, color,
				BitboardUtils.coordinates(from), 
				BitboardUtils.coordinates(to),
				Option.<PieceType> none());
		ACEBoard copiedBoard = new ACEBoard(board, EngineUtils.otherToMove(color));
		copiedBoard.apply(move);
		return copiedBoard;
	}
}
