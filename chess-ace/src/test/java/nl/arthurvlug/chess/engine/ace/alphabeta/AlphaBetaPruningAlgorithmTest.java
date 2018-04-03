package nl.arthurvlug.chess.engine.ace.alphabeta;

import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.utils.EngineTestUtils;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.EngineConstants.BLACK;
import static nl.arthurvlug.chess.engine.EngineConstants.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlphaBetaPruningAlgorithmTest {
	// Moves in initial position
	private static final int M = 20;
//	private static final int M = 4;
	
	private AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator());
	
	@Test
	public void testNodesSearched1() {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard, 1);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched2() {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard, 2);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M*M + M, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched3() {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard, 3);
		assertEquals(376, algorithm.getCutoffs());
		assertEquals(9322, algorithm.getNodesEvaluated());
	}

	@Test
	public void testSelfCheckmate() {
		ACEBoard engineBoard = new ACEBoard(BLACK);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("b1"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("b8"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("b7"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("b4"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a8"));
		engineBoard.finalizeBitboards();

		/*  ♚♖......
			.♖......
			........
			........
			.♞......
			........
			♙♙......
			♔♖......  */
		final AceMove move = algorithm.think(engineBoard, 3);
		assertNull(move);
	}

	@Test
	public void testExpectToMate() {
		ACEBoard engineBoard = new ACEBoard(BLACK);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("b1"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("b4"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a8"));
		engineBoard.finalizeBitboards();

		/*  ♚.......
			........
			........
			........
			.♞......
			........
			♙♙......
			♔♖......  */
		final AceMove move = algorithm.think(engineBoard, 3);
		assertEquals("b4c2", move.toString());
		assertEquals(67, algorithm.getCutoffs());
		assertEquals(870, algorithm.getNodesEvaluated());
	}

	@Test
	public void testWillLose() {
		ACEBoard engineBoard = new ACEBoard(BLACK);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("g7"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("h7"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a8"));
		engineBoard.finalizeBitboards();

		/*  ♚.......
			......♖♖
			........
			........
			........
			........
			........
			♔.......  */
		final AceMove move = algorithm.think(engineBoard, 7);
		assertNull(move);
	}

	@Test
	public void testMateIn2() {
		ACEBoard engineBoard = new ACEBoard(WHITE);
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a8"));
		engineBoard.addPiece(BLACK, ROOK, BitboardUtils.toIndex("b8"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("d7"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("c6"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("a6"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("b7"));

		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, ROOK, BitboardUtils.toIndex("a5"));
		engineBoard.addPiece(WHITE, QUEEN, BitboardUtils.toIndex("b5"));

		engineBoard.finalizeBitboards();

		/*  ♚♜......
			.♟.♟....
			♟.♟.....
			♖♕......
			........
			........
			........
			♔.......

		*/
		final AceMove move = algorithm.think(engineBoard, 5);
		assertEquals("a5a6", move.toString());
	}

	@Test
	public void testTakePieceWhite() {
		ACEBoard engineBoard = new ACEBoard(WHITE);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(BLACK, BISHOP, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("a1b2"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testTakePieceBlack() {
		ACEBoard engineBoard = new ACEBoard(BLACK);
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("a1b2"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testPieceProtected() {
		ACEBoard engineBoard = new ACEBoard(WHITE);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("c3"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("d4"));
		engineBoard.finalizeBitboards();
		
		/*	........
			........
			........
			........
			...♚....
			..♟.....
			.♔......
			........  */
		
		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("b2a2"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testCantTakePieceBecauseOfPenning() {
		ACEBoard engineBoard = new ACEBoard(WHITE);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(BLACK, ROOK, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();

		/*	.......♚
			........
			........
			........
			..♞.....
			........
			♔♘♜.....
			..♙.....  */

		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("a2b3"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testCanTakePieceBecauseNoPenning() {
		ACEBoard engineBoard = new ACEBoard(WHITE);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(BLACK, ROOK, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();

		/*	.......♚
			........
			........
			........
			..♞.....
			........
			.♘♜.....
			♔.♙.....  */

		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("b2c4"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testStartPosition() {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		
		AceMove bestMove = algorithm.think(engineBoard, 2);
		assertEquals(MoveUtils.toMove("b1a3"), EngineTestUtils.engineMoveToMove(bestMove));
	}

	@Test
	public void testQueenTakeMove() {
		ACEBoard engineBoard = new ACEBoard(BLACK);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("f1"));
		engineBoard.addPiece(WHITE, QUEEN, BitboardUtils.toIndex("d2"));
		engineBoard.addPiece(WHITE, BISHOP, BitboardUtils.toIndex("d1"));

		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("c7"));
		engineBoard.addPiece(BLACK, QUEEN, BitboardUtils.toIndex("d8"));
		engineBoard.finalizeBitboards();

		/*
			...♛....
			..♚.....
			........
			........
			........
			........
			...♕....
			...♗.♔..
		 */
		AceMove bestMove = algorithm.think(engineBoard, 1);
		assertEquals(MoveUtils.toMove("d8d2"), EngineTestUtils.engineMoveToMove(bestMove));
	}
}
