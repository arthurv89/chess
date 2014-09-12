package nl.arthurvlug.chess.engine.ace.movegeneration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.utils.EngineTestUtils;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Test;

public class MoveGeneratorTest {
	@Test
	public void testKingMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, BitboardUtils.toIndex("a7"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a2")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b2")));
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(7, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("b7a8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7a6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7b8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7b6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7c8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7c7")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7c6")));
	}

	@Test
	public void testRookMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("h8"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("a4"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("h6"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, BitboardUtils.toIndex("f1"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(8, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a2")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a3")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1c1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1d1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1e1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1f1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("h8g8")));
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(17, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b1")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b7")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2b8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2a2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2c2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2d2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2e2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2f2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2g2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2h2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6g6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6g5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6h5")));
	}

	@Test
	public void testKnightMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("h8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("h6"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("a4"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b3")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1c2")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("h8g8")));
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(6, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("b2a4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2d3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2d1")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6g6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6g5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("h6h5")));
	}

	@Test
	public void testBishopMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("e2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("b7"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(0, whiteMoves.size());
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("b7a8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7c8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7a6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7c6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7d5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7e4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7f3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7g2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b7h1")));
	}

	@Test
	public void testQueenMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, BitboardUtils.toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, BitboardUtils.toIndex("e2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, BitboardUtils.toIndex("a8"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(0, whiteMoves.size());
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(21, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a7")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8a1")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8b8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8c8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8d8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8e8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8f8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8g8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8h8")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8b7")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8c6")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8d5")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8e4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8f3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8g2")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a8h1")));
	}

	@Test
	public void testBitboardCheckCantMoveToCheckField() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("a3"));
		engineBoard.finalizeBitboards();
		engineBoard.generateSuccessorBoards();

		
		List<Move> whiteMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE)));
		assertEquals(1, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b1")));
		
		List<Move> blackMoves = EngineTestUtils.engineMovesToMoves(MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK)));
		assertEquals(3, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("a3a4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a3b3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("a3b4")));
	}
}
