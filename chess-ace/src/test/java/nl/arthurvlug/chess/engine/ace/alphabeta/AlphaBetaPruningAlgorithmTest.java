package nl.arthurvlug.chess.engine.ace.alphabeta;

import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.BLACK;
import static nl.arthurvlug.chess.engine.ColorUtils.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlphaBetaPruningAlgorithmTest {
	// Moves in initial position
	private static final int M = 20;
//	private static final int M = 4;

	private AlphaBetaPruningAlgorithm algorithm;

	@Before
	public void before() {
		algorithm = new AlphaBetaPruningAlgorithm(new AceConfiguration());
	}

//	@Ignore
	@Test
	public void testNodesSearched1() {
		algorithm.setDepth(1);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched2() {
		algorithm.setDepth(2);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(19, algorithm.getCutoffs());
		assertEquals(59, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched3() {
		algorithm.setDepth(3);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		algorithm.think(engineBoard);
		assertEquals(57, algorithm.getCutoffs());
		assertEquals(583, algorithm.getNodesEvaluated());
	}

	@Test
	public void testSelfCheckmate() {
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("a2"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("b1"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("b8"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("b7"));
		engineBoard.addPiece(BLACK, KNIGHT, FieldUtils.fieldIdx("b4"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a8"));
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
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("a2"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("b1"));
		engineBoard.addPiece(BLACK, KNIGHT, FieldUtils.fieldIdx("b4"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a8"));
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
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("g7"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("h7"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a8"));
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
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a8"));
		engineBoard.addPiece(BLACK, ROOK, FieldUtils.fieldIdx("b8"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("d7"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("c6"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("a6"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("b7"));

		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, ROOK, FieldUtils.fieldIdx("a5"));
		engineBoard.addPiece(WHITE, QUEEN, FieldUtils.fieldIdx("b5"));

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
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(BLACK, BISHOP, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("h8"));
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), bestMove);
	}

	@Test
	public void testTakePieceBlack() {
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("h8"));
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), bestMove);
	}

	@Test
	public void testPieceProtected() {
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("c3"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("d4"));
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
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a2"));
		engineBoard.addPiece(WHITE, KNIGHT, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("c1"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("c3"));
		engineBoard.addPiece(BLACK, KNIGHT, FieldUtils.fieldIdx("c4"));
		engineBoard.addPiece(BLACK, ROOK, FieldUtils.fieldIdx("c2"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("h8"));
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
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, PAWN, FieldUtils.fieldIdx("c1"));
		engineBoard.addPiece(BLACK, PAWN, FieldUtils.fieldIdx("c3"));
		engineBoard.addPiece(BLACK, KNIGHT, FieldUtils.fieldIdx("c4"));
		engineBoard.addPiece(BLACK, ROOK, FieldUtils.fieldIdx("c2"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("h8"));
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
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		
		Move bestMove = algorithm.think(engineBoard);
		assertEquals(MoveUtils.toMove("b1c3"), bestMove);
	}

	@Test
	public void testQueenTakeMove() {
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("f1"));
		engineBoard.addPiece(WHITE, QUEEN, FieldUtils.fieldIdx("d2"));
		engineBoard.addPiece(WHITE, BISHOP, FieldUtils.fieldIdx("d1"));

		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("c7"));
		engineBoard.addPiece(BLACK, QUEEN, FieldUtils.fieldIdx("d8"));
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
