package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.collect.ImmutableList;
import java.util.List;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.utils.ACEBoardUtils;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromBoard;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ACEBoardTest {
	private static final ACEBoard startPositionBoard = ACEBoardUtils.initializedBoard(WHITE, "" +
			"♟♞♝♜♛♚..\n" +
			".....♙..\n" +
			"........\n" +
			"........\n" +
			"........\n" +
			"........\n" +
			"........\n" +
			"♙♘♗♖♕♔..\n");

	@Before
	public void before() {
		AceConfiguration.DEBUG = true;
	}

	@Test
	public void testInvalidRookMove() throws Exception {
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		final List<String> moves = ImmutableList.of(
				"d2d4", "d7d5", "b1c3", "b8c6", "c1f4", "c8f5", "c3b5", "a8c8",
				"g1f3", "e7e6", "b5c7", "c8c7", "f4c7", "d8c7", "e2e3", "c7b6",
				"d1c1", "c6b4", "f1d3", "f5d3", "e1d2", "d3e4", "c2c4", "b4c2",
				"a1b1", "f8b4", "d2d1", "c2e3", "f2e3", "e4b1", "c4c5", "b6a5",
				"c1b1", "g8f6", "a2a3", "a5a4", "d1c1", "b4a5", "b1d3", "f6e4",
				"h1f1", "a5c7", "c1b1", "b7b6", "c5b6", "c7b6", "f3d2", "f7f5",
				"d2f3", "f5f4", "e3f4");
		engineBoard.apply(moves);
		AceConfiguration configuration = new AceConfiguration();
		configuration.setSearchDepth(2);
		final AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(configuration);
		final Move move = algorithm.think(engineBoard);
		assertThat(move.toString()).isNotEqualTo("f8f4");
	}

	@Test
	public void testStartingPosition() throws Exception {
		verifyBitboards(startPositionBoard);
	}

	@Test
	public void testAfterPlayingMoves() {
		final ACEBoard copyBoard = startPositionBoard.cloneBoard(ColorUtils.BLACK, false);
		copyBoard.apply(ImmutableList.of("f8f7"));
		final ACEBoard blackToMove = copyBoard.cloneBoard(opponent(copyBoard.toMove), false);
		verifyCopyBoard(blackToMove);
	}

	private void verifyBitboards(final ACEBoard startPositionBoard) {
		assertEquals(startPositionBoard.white_pawns, bitboardFromFieldName("a1 f7"));
		assertEquals(startPositionBoard.white_knights, bitboardFromFieldName("b1"));
		assertEquals(startPositionBoard.white_bishops, bitboardFromFieldName("c1"));
		assertEquals(startPositionBoard.white_rooks, bitboardFromFieldName("d1"));
		assertEquals(startPositionBoard.white_queens, bitboardFromFieldName("e1"));
		assertEquals(startPositionBoard.white_kings, bitboardFromFieldName("f1"));

		assertEquals(startPositionBoard.black_pawns, bitboardFromFieldName("a8"));
		assertEquals(startPositionBoard.black_knights, bitboardFromFieldName("b8"));
		assertEquals(startPositionBoard.black_bishops, bitboardFromFieldName("c8"));
		assertEquals(startPositionBoard.black_rooks, bitboardFromFieldName("d8"));
		assertEquals(startPositionBoard.black_queens, bitboardFromFieldName("e8"));
		assertEquals(startPositionBoard.black_kings, bitboardFromFieldName("f8"));

		assertEquals(startPositionBoard.enemy_and_empty_board, bitboardFromBoard(
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟.♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"......♟♟\n"));

		assertEquals(startPositionBoard.occupiedSquares[ColorUtils.WHITE], bitboardFromBoard(
				"........\n" +
				".....♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♟♟♟♟♟♟..\n"));

		assertEquals(startPositionBoard.occupiedSquares[ColorUtils.BLACK], bitboardFromBoard(
				"♟♟♟♟♟♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n"));
	}

	private void verifyCopyBoard(final ACEBoard copyBoard) {
		assertEquals(copyBoard.white_pawns, bitboardFromFieldName("a1"));
		assertEquals(copyBoard.white_knights, bitboardFromFieldName("b1"));
		assertEquals(copyBoard.white_bishops, bitboardFromFieldName("c1"));
		assertEquals(copyBoard.white_rooks, bitboardFromFieldName("d1"));
		assertEquals(copyBoard.white_queens, bitboardFromFieldName("e1"));
		assertEquals(copyBoard.white_kings, bitboardFromFieldName("f1"));

		assertEquals(copyBoard.black_pawns, bitboardFromFieldName("a8"));
		assertEquals(copyBoard.black_knights, bitboardFromFieldName("b8"));
		assertEquals(copyBoard.black_bishops, bitboardFromFieldName("c8"));
		assertEquals(copyBoard.black_rooks, bitboardFromFieldName("d8"));
		assertEquals(copyBoard.black_queens, bitboardFromFieldName("e8"));
		assertEquals(copyBoard.black_kings, bitboardFromFieldName("f7"));

		assertEquals(copyBoard.enemy_and_empty_board, bitboardFromBoard(
				".....♟♟♟\n" +
				"♟♟♟♟♟.♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n" +
				"♟♟♟♟♟♟♟♟\n"));

		assertEquals(copyBoard.occupiedSquares[ColorUtils.WHITE], bitboardFromBoard(
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♟♟♟♟♟♟..\n"));

		assertEquals(copyBoard.occupiedSquares[ColorUtils.BLACK], bitboardFromBoard(
				"♟♟♟♟♟...\n" +
				".....♟..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n"));
	}

	@Test
	public void testUnapplyPawnMove() {
		final ACEBoard oldBoard = ACEBoardUtils.initializedBoard(WHITE, "" +
				"♟♞♝♜♛♚..\n" +
				".....♙..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♙♘♗♖♕♔..\n");


		Integer move = UnapplyableMoveUtils.createMove("f7e8", oldBoard);

		final ACEBoard newBoard = startPositionBoard.cloneBoard();
		newBoard.apply(move);
		newBoard.unapply(move);
		assertEquals(ACEBoardUtils.dump(oldBoard), ACEBoardUtils.dump(newBoard));
	}

	@Test
	public void testUnapplyTakeMove() throws Exception {
		verifyBitboards(startPositionBoard);
	}

}
