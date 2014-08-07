package nl.arthurvlug.chess.engine.ace.alphabeta;

import static org.junit.Assert.assertEquals;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.utils.EngineTestUtils;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import org.junit.Test;

public class AlphaBetaPruningAlgorithmTest {
	private AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator());

	@Test
	public void testTakePieceWhite() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testTakePieceBlack() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.BLACK);
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testCantTakePiece() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, BitboardUtils.toIndex("c3"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("d4"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("b2a1"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testCantTakePiecePiece() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a2a1"), EngineTestUtils.engineMoveToMove(bestMove));
	}
}
