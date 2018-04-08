package nl.arthurvlug.chess.engine.ace.alphabeta;

import nl.arthurvlug.chess.engine.ace.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.EngineConstants.BLACK;
import static nl.arthurvlug.chess.engine.EngineConstants.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlphaBetaPruningAlgorithmTest {
	// Moves in initial position
	private static final int M = 20;
//	private static final int M = 4;

	private AlphaBetaPruningAlgorithm<ACEBoard> algorithm;

	@Before
	public void before() {
		algorithm = new AlphaBetaPruningAlgorithm<>(new AceConfiguration());
	}

//	@Ignore
	@Test
	public void testNodesSearched1() {
		algorithm.setDepth(1);
		algorithm.setEvaluator(new SimplePieceEvaluator());
		algorithm.setQuiesceEnabled(false);
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M, algorithm.getNodesEvaluated());
	}
	@Test
	public void testNodesSearched2() {
		algorithm.setDepth(2);
		algorithm.setEvaluator(new SimplePieceEvaluator());
		algorithm.setQuiesceEnabled(false);
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M + M*M, algorithm.getNodesEvaluated());
	}

//	@Ignore
	@Test
	public void testNodesSearched3() {
		algorithm.setDepth(3);
		algorithm.setEvaluator(new SimplePieceEvaluator());
		algorithm.setQuiesceEnabled(false);
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(M*M-1, algorithm.getCutoffs());
		assertEquals(839, algorithm.getNodesEvaluated());
	}

	@Test
	public void testSelfCheckmate() {
		ACEBoard engineBoard = new ACEBoard(BLACK, false);
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
		final Move move = algorithm.think(engineBoard);
		assertNull(move);
	}

	@Test
	public void testExpectToMate() {
		ACEBoard engineBoard = new ACEBoard(BLACK, false);
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
		final Move move = algorithm.think(engineBoard);
		assertEquals("b4c2", move.toString());
	}

	@Test
	public void testWillLose() {
		ACEBoard engineBoard = new ACEBoard(BLACK, false);
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
		algorithm.setDepth(3);
		final Move move = algorithm.think(engineBoard);
		assertNull(move);
	}

	@Test
	public void testMateIn2() {
		ACEBoard engineBoard = new ACEBoard(WHITE, false);
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
		algorithm.setDepth(5);
		final Move move = algorithm.think(engineBoard);
		assertEquals("a5a6", move.toString());
	}

	@Test
	public void testTakePieceWhite() {
		ACEBoard engineBoard = new ACEBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(BLACK, BISHOP, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), bestMove);
	}

	@Test
	public void testTakePieceBlack() {
		ACEBoard engineBoard = new ACEBoard(BLACK, false);
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), bestMove);
	}

	@Test
	public void testPieceProtected() {
		ACEBoard engineBoard = new ACEBoard(WHITE, false);
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
		
		Move bestMove = algorithm.think(engineBoard);
		assertThat(bestMove).isNotEqualTo(MoveUtils.toMove("b2c3"));
	}

	@Test
	public void testCantTakePieceBecauseOfPin() {
		ACEBoard engineBoard = new ACEBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("c3"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(BLACK, ROOK, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();

		/*	.......♚
			........
			........
			........
			..♞.....
			..♟.....
			♔♘♜.....
			..♙.....  */

		Move bestMove = algorithm.think(engineBoard);
		assertThat(bestMove);
	}

	@Test
	public void testCanTakePieceBecauseNoPin() {
		ACEBoard engineBoard = new ACEBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(WHITE, PAWN, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(BLACK, PAWN, BitboardUtils.toIndex("c3"));
		engineBoard.addPiece(BLACK, KNIGHT, BitboardUtils.toIndex("c4"));
		engineBoard.addPiece(BLACK, ROOK, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(BLACK, KING, BitboardUtils.toIndex("h8"));
		engineBoard.finalizeBitboards();

		/*	.......♚
			........
			........
			........
			..♞.....
			..♟.....
			.♘♜.....
			♔.♙.....  */

		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("b2c4"), bestMove);
	}

	@Test
	public void testStartPosition() {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("b1c3"), bestMove);
	}

	@Test
	public void testQueenTakeMove() {
		ACEBoard engineBoard = new ACEBoard(BLACK, false);
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
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("d8d2"), bestMove);
	}
}
