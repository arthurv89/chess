package nl.arthurvlug.chess.engine.ace;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.function.Function;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.BLACK;
import static nl.arthurvlug.chess.engine.ColorUtils.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class AlphaBetaPruningAlgorithmTest {
	// Moves in initial position
	private static final int M = 20;
//	private static final int M = 4;

	private AlphaBetaPruningAlgorithm algorithm;

	@Before
	public void before() {
		algorithm = new AlphaBetaPruningAlgorithm(new AceConfiguration());
		AceConfiguration.DEBUG = true;
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
		// Expect d2d4 because pawns are found first, and they come in order of fieldIdx
		// Moves with the same score are: e2e4, b1c3 and g1f3
		assertEquals(MoveUtils.toMove("d2d4"), bestMove);
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


	@Test
	public void testTrap_Depth1() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 1); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 2);
	}

	@Test
	public void testTrap_Depth1_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 1); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 2);
	}

	@Test
	public void testDontLetOpponentTakeKnight() {
		checkMove("e2e4 g8f6 e4e5", movesPiece("f6"), 2);
	}

	@Test
	public void testShouldMoveKnight_white() {
		checkMove("g1f3 e7e5 b2b3 e5e4", movesPiece("f3"), 2);
	}

	@Ignore
	@Test
	public void testShouldPlayWinningMove() { // e6 fxe6 Ne5 Bxc3 bxc3 [...] Nxd7
//		.♜.♛♚..♜
//		..♟♞♞♟♟♟
//		........
//		♟...♙...
//		♕♝......
//		..♘.♗♘..
//		♙♙...♙♙♙
//		..♔♖...♖

		// Expect to play e5e6
		checkMove("d2d4 b8c6 b1c3 g8f6 g1f3 d7d5 e2e3 c8f5 c1d2 e7e6 f1d3 f5d3 c2d3 f8d6 d1b3 a8b8 e3e4 d5e4 d3e4 e6e5 d4e5 c6e5 b3a4 e5c6 d2e3 a7a5 e4e5 b7b5 c3b5 d6b4 e1c1 f6d7 b5c3 c6e7",
				movesPiece("e5"), 6);
	}

	private void checkMove(String sMoves, Function<Move, Boolean> expect, int depth) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		checkMove(moves, expect, depth);
	}

	private void checkMove(final List<String> moves, final Function<Move, Boolean> expect, final int depth) {
		ACE ace = new ACE(depth, new SimplePieceEvaluator(), 1);
		Move move = ace.think(moves, new ThinkingParams());
		assertTrue(expect.apply(move));
	}

	private Function<Move, Boolean> is(String string) {
		return move -> move.toString().equals(string);
	}

	private Function<Move, Boolean> movesPiece(String fromField) {
		return move -> move.toString().startsWith(fromField);
	}

	private Function<Move, Boolean> not(Function<Move, Boolean> function) {
		return move -> !function.apply(move);
	}
}
