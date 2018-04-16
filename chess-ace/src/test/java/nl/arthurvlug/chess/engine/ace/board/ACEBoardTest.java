package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.collect.ImmutableList;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.utils.ACEBoardUtils;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromBoard;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
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
