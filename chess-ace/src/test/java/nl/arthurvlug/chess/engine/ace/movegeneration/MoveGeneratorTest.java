package nl.arthurvlug.chess.engine.ace.movegeneration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;

import org.junit.Test;

public class MoveGeneratorTest {
	@Test
	public void testKingMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("a7"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE));
		List<Move> blackMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK));
		
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a2")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b2")));
		
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
	public void testKnightMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("a4"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE));
		List<Move> blackMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK));
		
		assertEquals(2, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b3")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1c2")));
		
		assertEquals(3, blackMoves.size());
		assertTrue(blackMoves.contains(BitboardUtils.move("b2a4")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2d3")));
		assertTrue(blackMoves.contains(BitboardUtils.move("b2d1")));
	}

	@Test
	public void testRookMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("f1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("a4"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE));
		List<Move> blackMoves = MoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK));
		
		assertEquals(7, whiteMoves.size());
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a2")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1a3")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1b1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1c1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1d1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1e1")));
		assertTrue(whiteMoves.contains(BitboardUtils.move("a1f1")));
		
		assertEquals(14, blackMoves.size());
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
	}
}
