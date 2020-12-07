package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import java.util.List;
import java.util.function.Function;
import nl.arthurvlug.chess.engine.ace.ACE;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.BLACK;
import static nl.arthurvlug.chess.engine.ColorUtils.WHITE;
import static nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration.DEFAULT_QUIESCE_MAX_DEPTH;
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
		MoveUtils.DEBUG = false;
		AceConfiguration configuration = new AceConfiguration();
		configuration.setQuiesceMaxDepth(3);
		algorithm = new AlphaBetaPruningAlgorithm(configuration);
		algorithm.setEventBus(new EventBus());
	}

//	@Ignore
	@Test
	public void testNodesSearched1() {
		algorithm.setDepth(1);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuiesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		getAceResponse(engineBoard);
		assertEquals(0, algorithm.getCutoffs());
		assertEquals(M, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched2() {
		algorithm.setDepth(2);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuiesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		getAceResponse(engineBoard);
		assertEquals(19, algorithm.getCutoffs());
		assertEquals(59, algorithm.getNodesEvaluated());
	}

	@Test
	public void testNodesSearched3() {
		algorithm.setDepth(3);
		algorithm.useSimplePieceEvaluator();
		algorithm.disableQuiesce();
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.finalizeBitboards();
		getAceResponse(engineBoard);
		assertEquals(57, algorithm.getCutoffs());
		assertEquals(583, algorithm.getNodesEvaluated());
	}

	@Test
	public void testSelfCheckmate() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.BLACK, "" +
				"♚♖......\n" +
				".♖......\n" +
				"........\n" +
				"........\n" +
				".♞......\n" +
				"........\n" +
				"♙♙......\n" +
				"♔♖......");

		/*    */
		final Move move = getAceResponse(engineBoard);
		System.out.println(move);
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
		final Move move = getAceResponse(engineBoard);

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
		algorithm.disableQuiesce();
		final Move move = getAceResponse(engineBoard);

		assertThat(move.toString()).isEqualTo("a8b8");
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
		final Move move = getAceResponse(engineBoard);
		assertEquals("a5a6", move.toString());
	}

	@Test
	public void testTakePieceWhite() {
		/*  .......♚
			........
			........
			........
			........
			........
			.♝......
			♔.......
		*/
		ACEBoard engineBoard = ACEBoard.emptyBoard(WHITE, false);
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(BLACK, BISHOP, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), move);
	}

	@Test
	public void testTakePieceBlack() {
		ACEBoard engineBoard = ACEBoard.emptyBoard(BLACK, false);
		engineBoard.addPiece(BLACK, KING, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(WHITE, KNIGHT, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(WHITE, KING, FieldUtils.fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("a1b2"), move);
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

		final Move move = getAceResponse(engineBoard);
		assertThat(move).isNotEqualTo(MoveUtils.toMove("b2c3"));
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

		final Move move = getAceResponse(engineBoard);
		assertThat(move);
	}

	private Move getAceResponse(final ACEBoard engineBoard) {
		return algorithm.startThinking(engineBoard).toBlocking().first();
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

		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("b2c4"), move);
	}

	@Test
	public void testStartPosition() {
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();

		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("b1c3"), move);
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
		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("d8d2"), move);
	}

	@Test
	public void testShouldPromoteToQueenAndWin() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.WHITE, "" +
				".......♚\n" +
				"...♙..♟♟\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				".....♔..\n");
//		algorithm.useSimplePieceEvaluator();
//		algorithm.disableQuiesce();
		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("d7d8q"), move);
	}

	@Test
	public void testShouldPromoteToKnightAndWin() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.WHITE, "" +
				".....♔..\n" +
				".....♟♙♟\n" +
				"......♟♚\n" +
				"......♟♟\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n");
		algorithm.setDepth(5);
		final Move move = getAceResponse(engineBoard);
		assertEquals(MoveUtils.toMove("g7g8n"), move);
	}

	@Test
	public void shouldNotGiveAwayRook() {
		final List<String> moves = ImmutableList.of("e2e4", "d7d5", "e4d5", "g8f6", "d2d4", "f6d5", "g1f3", "b8c6", "c2c4", "d5f6", "b1c3", "c8g4", "d4d5", "g4f3", "d1f3", "c6e5", "f3d1", "e7e6", "c1f4", "f8d6", "f4e5", "d6e5", "d5e6", "d8d1", "a1d1", "f7e6", "f1e2", "e8g8", "e1g1", "e5c3", "b2c3", "f6e4", "e2g4", "e6e5", "d1d7", "a8c8", "d7e7", "c8b8", "e7e5", "e4c3", "e5e7", "c7c5", "e7c7", "c3e4", "f2f3", "e4c3", "f1e1", "g8h8", "e1e7", "b8d8", "e7g7", "d8d1", "g1f2", "c3e4", "f2e2", "e4c3", "e2f2", "c3e4", "f2e3", "d1e1", "e3d3", "f8d8", "c7d7", "d8d7", "g7d7", "e4f6", "d7b7");
		final ACEBoard engineBoard = createEngineBoard(moves);
		algorithm.setDepth(5);
		final Move move = getAceResponse(engineBoard);
		assertThat(move.toString()).isNotEqualTo("e1b1");
	}

	@Test
	public void shouldCreateMove() {
		final List<String> moves = ImmutableList.of("e2e4", "d7d5", "e4d5", "g8f6", "b1c3", "f6d5", "d1f3", "e7e6", "f1c4", "d5b4", "c4b3", "b8c6", "g1e2", "f8c5", "e1g1", "e8g8", "d2d3", "c6a5", "f3g3", "a5b3", "a2b3", "b4c2", "c1h6");
		final ACEBoard engineBoard = createEngineBoard(moves);
		algorithm.setDepth(3);
		getAceResponse(engineBoard);// Should not throw an exception
	}

	private ACEBoard createEngineBoard(final List<String> moves) {
		final ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.apply(moves);
		return engineBoard;
	}

	@Test
	public void shouldNotMoveKing() {
		final List<String> g3Moves = ImmutableList.copyOf("e2e4 d7d5 e4e5 b8c6 d2d4 e7e6 f2f4 d8h4 g2g3".split(" "));
		final ACEBoard g3EngineBoard = createEngineBoard(g3Moves);
		final Integer g3Score = new AceEvaluator().evaluate(g3EngineBoard);
		assertThat(g3Score).isEqualTo(-50);

		final List<String> ke2Moves = ImmutableList.copyOf("e2e4 d7d5 e4e5 b8c6 d2d4 e7e6 f2f4 d8h4 e1e2".split(" "));
		final ACEBoard ke2EngineBoard = createEngineBoard(ke2Moves);
		final Integer ke1Score = new AceEvaluator().evaluate(ke2EngineBoard);
		assertThat(ke1Score).isEqualTo(-35);

		final List<String> beforeMoves = ImmutableList.copyOf("e2e4 d7d5 e4e5 b8c6 d2d4 e7e6 f2f4 d8h4".split(" "));
		final ACEBoard beforeEngineBoard = createEngineBoard(beforeMoves);

		algorithm.setDepth(1);
		final Move move = getAceResponse(beforeEngineBoard);// Should not throw an exception
		assertThat(move.toString()).isEqualTo("e1e2");
	}

//	@Test
//	public void shouldNotStalemate() {
//		// e2e4, d7d5, g1f3, d5e4, f1b5, c7c6, b5e2, e4f3, e2f3, e7e5, e1g1, g8f6, d2d3, c8f5, c1g5, f8c5, f1e1, b8d7, b1c3, e8g8, c3e4, d8e7, e4f6, d7f6, d1e2, a8e8, f3h5, c5d4, c2c3, d4c5, d3d4, c5d6, d4e5, e7e5, f2f4, e5c5, e2f2, e8e1, a1e1, c5f2, g1f2, f6h5, g2g4, f5g4, h2h3, g4h3, e1d1, d6f4, g5f4, h5f4, f2g3, g7g5, d1h1, h3e6, h1f1, e6a2, g3g4, h7h6, f1h1, a2e6, g4g3, h6h5, h1e1, f4d3, e1e2, d3f4, e2e5, f7f6, e5e1, f6f5, c3c4, e6c4, b2b4, f4d5, e1e5, d5b4, e5e1, b4d3, e1h1, f5f4, g3f3, c4d5, f3e2, d5h1, e2d3, h1d5, d3d4, g5g4, d4e5, g4g3, e5d6, g3g2, d6c7, f8f7, c7b8, g2g1q, b8c8, h5h4, c8d8, h4h3, d8e8
//		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.BLACK, "" +
//				"....♔.♚.\n" +
//				"♟♟...♜..\n" +
//				"..♟.....\n" +
//				"...♝....\n" +
//				".....♟..\n" +
//				".......♟\n" +
//				"........\n" +
//				"......♛.");
//
//		algorithm.setDepth(3);
//		final Move move = algorithm.startThinking(engineBoard);
//		assertThat(move.toString()).isNotEqualTo("g1b6");
//	}

	@Test
	public void checkStalemate() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.BLACK, "" +
				"....♚..\n" +
				"....♙...\n" +
				"....♔...\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n");

		algorithm.setDepth(2);
		final Move move = getAceResponse(engineBoard);
		assertThat(move).isNull();
	}

	@Test
	public void checkCanStalemateButShouldNot() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.WHITE, "" +
				"....♚...\n" +
				"....♙...\n" +
				"........\n" +
				".....♔..\n" +
				"........\n" +
				"........\n" +
				"....♙...\n" +
				"........\n");

		algorithm.setDepth(4);
		final Move move = getAceResponse(engineBoard);
		assertThat(move.toString()).isNotEqualTo("f5e6");
	}

	@Test
	public void checkWhiteMated() {
		final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.WHITE, "" +
				"♔.......\n" +
				".♛......\n" +
				"..♚.....\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n");

		algorithm.setDepth(3);
		final Move move = getAceResponse(engineBoard);
		assertThat(move).isNull();
	}

	@Test
	public void shouldCheckMateWithPawn() {
		final ACEBoard engineBoard = createEngineBoard("b1c3 d7d5 e2e4 e7e6 e4d5 e6d5 d2d4 g8f6 g1f3 c8g4 d1e2 f8e7 e2b5 b8c6 b5b7 g4d7 f1b5 a8b8 b7a6 b8b6 a6a4 a7a6 b5e2 c6b4 a4b3 b4d3 e2d3 b6b3 a2b3 c7c5 a1a6 c5d4 f3d4 e7c5 c1e3 d8e7 a6a8 d7c8 a8c8 e8d7 d3f5 d7d6 c3b5 d6e5");
		algorithm.setDepth(3);
		final Move move = getAceResponse(engineBoard);
		assertThat(move.toString()).isEqualTo("f2f4");
	}

	@Test
	public void shouldMoveWhenGettingCheckmated() {
		final ACEBoard engineBoard = createEngineBoard("e2e4 g8f6 b1c3 b8c6 f2f4 d7d5 e4e5 d5d4 c3b5 f6d5 g1f3 d5f4 d2d3 f4g6 f1e2 c8e6 e1g1 a7a6 b5a3 g6e5 f3e5 c6e5 a3c4 e5c4 d3c4 d8d6 b2b4 e8c8 c4c5 d6d5 e2f3 d5c4 a2a3 h7h6 f3e2 c4d5 c1f4 c8b8 e2f3 d5c4 c5c6 b7c6 f3e2 c4d5 e2a6 e6f5 a1b1 e7e5 f4g3 f5e6 d1d3 e5e4 d3e2 e4e3 b4b5 c6c5 b5b6 f8d6 b6c7 b8c7 b1b7 d5b7 a6b7 d6g3 e2b5 g3d6 b5c6 c7b8 f1b1 e6a2 b1b2 a2e6 b7a6");
		algorithm.setDepth(3);
		final Move move = getAceResponse(engineBoard);
		assertThat(move.toString()).isEqualTo("b8a7");
	}

	@Test
	public void testTrap_Depth1() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 1, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 2, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void testTrap_Depth1_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 1, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 2, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void testDontLetOpponentTakeKnight() {
		checkMove("e2e4 g8f6 e4e5", movesPiece("f6"), 2, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void testShouldMoveKnight_white() {
		checkMove("g1f3 e7e5 b2b3 e5e4", movesPiece("f3"), 2, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void testShouldNotPutKingInCheckAfterPromotion() {
		checkMove("d2d4 d7d5 b1c3 b8c6 c1f4 c8f5 c3b5 a8c8 g1f3 e7e6 b5c7 c8c7 f4c7 d8c7 e2e3 c7b6 d1c1 c6b4 f1d3 f5d3 e1d2 d3e4 c2c4 b4c2 a1b1 f8b4 d2d1 c2e3 f2e3 e4b1 c4c5 b6a5 c1b1 g8f6 a2a3 a5a4 d1c1 b4a5 b1a2 e8g8 c1b1 a4b5 f3e5 a5d2 a3a4 b5e2 b2b3 e2g2 h1c1 g2e4 a2c2 d2c1 c2e4 d5e4 b1c1 f6d5 e5c4 b7b6 c5b6 a7b6 c1b2 f8b8 c4d2 d5e3 d2e4 b8d8 b2c3 e3f5 c3b2 d8d4 e4g3 f5g3 h2g3 d4g4 b2b1 g4g3 b1a2 e6e5 a2b2 f7f5 b2a2 f5f4 b3b4 f4f3 a4a5 f3f2 a5b6 f2f1q b6b7 g3g2",
				  not(is("a2a1")), 1, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);

	}

	@Test
	public void shouldNotGetStuck() {
		checkMove("d2d4 d7d5 b1c3 b8c6 c1f4 c8f5 c3b5 a8c8 g1f3 e7e6 b5c7 c8c7 f4c7 d8c7 e2e3 c7b6 d1c1 c6b4 f1d3 f5d3 e1d2 d3e4 c2c4 b4c2 a1b1 f8b4 d2d1 c2e3 f2e3 e4b1 c4c5 b6a5 c1b1 g8f6 a2a3 a5a4 d1c1 b4a5 b1a2 e8g8 c1b1 a4b5 f3e5 a5d2 a3a4 b5e2 b2b3 e2g2 h1c1 g2e4 a2c2 d2c1 c2e4 d5e4 b1c1 f6d5 e5c4 b7b6 c5b6 a7b6 c1b2 f8b8 c4d2 d5e3 d2e4 b8d8 b2c3 e3f5 c3b2 d8d4 e4g3 f5g3 h2g3 d4g4 b2b1 g4g3 b1a2",
				not(is("a1a1")), 4, new AceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void blackShouldNotCastleWhenInCheck() {
		checkMove("d2d4 d7d5 c2c4 d5c4 e2e3 b7b5 a2a4 c8a6 a4b5 a6b5 b1a3 b5c6 a3c4 g8f6 g1f3 b8d7 c4e5 c6f3 d1f3 d7e5 d4e5 f6d7 f1b5 e7e6 e1g1 f8c5 b5d7",
				not(is("e8g8")), 3, new SimplePieceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

	@Test
	public void whiteShouldNotCastleWhenInCheck() {
		checkMove("d2d4 d7d5 b1c3 g8f6 g1f3 c8g4 c1f4 c7c6 e2e3 d8b6 a1b1 e7e6 f1d3 f8b4 a2a3 b4c3 b2c3 b6a5 b1b7 a5c3",
				not(is("e1g1")), 1, new AceEvaluator(), DEFAULT_QUIESCE_MAX_DEPTH);
	}

//	@Test
//	public void whiteShouldNotCastleWhenInCheck() throws KingEatingException {
//		final ACEBoard board = createEngineBoard("d2d4 d7d5 b1c3 g8f6 g1f3 c8g4 c1f4 c7c6 e2e3 d8b6 a1b1 e7e6 f1d3 f8b4 a2a3 b4c3 b2c3 b6a5 b1b7 a5c3");
//		final List<Integer> moves = board.generateMoves();
//		System.out.println(UnapplyableMoveUtils.listToString(moves));
//	}

	private ACEBoard createEngineBoard(final String movesString) {
		final List<String> moves = ImmutableList.copyOf(movesString.split(" "));
		return createEngineBoard(moves);
	}

	@Test
	public void shouldTakeBackQueen() {
		checkMove("e2e4 d7d5 e4d5 d8d5 b1c3 d5e6 d1e2 b8c6 b2b3 c6d4 e2e6",
				not(is("a8b8")), 3, new AceEvaluator(), 0);
	}

	@Test
	public void whiteShouldMate() {
		checkMove("d2d4 d7d5 b1c3 b8c6 c1f4 g8f6 c3b5 g7g6 b5c7 e8d7 c7a8 d8a5 c2c3 f6e8 g1f3 e8d6 e2e3 f8g7 f1d3 b7b6 f3e5 c6e5 d4e5 d6f5 b2b4 a5a3 d3b5 d7e6",
				is("a8c7"), 4, new AceEvaluator(), 6);
	}

	@Test
	public void blackShouldNotMoveBecauseItsCheckmate() {
		checkMove("d2d4 d7d5 b1c3 b8c6 c1f4 g8f6 c3b5 g7g6 b5c7 e8d7 c7a8 d8a5 c2c3 f6e8 g1f3 e8d6 e2e3 f8g7 f1d3 b7b6 f3e5 c6e5 d4e5 d6f5 b2b4 a5a3 d3b5 d7e6 a8c7",
				m -> m == null, 4, new AceEvaluator(), 6);
	}

	@Test
	public void shouldNotMoveBecauseTheKingCanBeEaten() {
		checkMove("d2d4 d7d5 b1c3 b8c6 c1f4 g8f6 c3b5 g7g6 b5c7 e8d7 c7a8 d8a5 c2c3 f6e8 g1f3 e8d6 e2e3 f8g7 f1d3 b7b6 f3e5 c6e5 d4e5 d6f5 b2b4 a5a3 d3b5 d7e6 a8c7 a3c3",
				m -> m == null, 4, new AceEvaluator(), 6);
	}

	private void checkMove(String sMoves, Function<Move, Boolean> expect, int depth, final BoardEvaluator evaluator, final int quiesceMaxDepth) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		checkMove(moves, expect, depth, evaluator, quiesceMaxDepth);
	}

	private void checkMove(final List<String> moves, final Function<Move, Boolean> expect, final int depth, final BoardEvaluator evaluator, final int quiesceMaxDepth) {
		final ACEBoard aceBoard = createEngineBoard(moves);
		final ACE ace = new ACE(depth, evaluator, quiesceMaxDepth, "ACE", new EventBus(), aceBoard);
		final Move move = ace.startThinking().toBlocking().first();
		System.out.println("Move is: " + move);
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
