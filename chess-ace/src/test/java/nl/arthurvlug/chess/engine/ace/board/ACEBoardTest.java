package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.collect.ImmutableList;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.utils.ACEBoardUtils;
import org.junit.Test;

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

	@Test
	public void testStartingPosition() throws Exception {
		verifyBitboards(startPositionBoard);
	}

	@Test
	public void testAfterPlayingMoves() {
		final ACEBoard copyBoard = startPositionBoard.cloneBoard();
		copyBoard.apply(ImmutableList.of(
				"a1a2",
				"a8a7",
				"b1b2",
				"b8b7",
				"c1c2",
				"c8c7",
				"d1d2",
				"d8d7",
				"e1e2",
				"e8e7",
				"f1f2",
				"f8f7"));
		final ACEBoard blackToMove = copyBoard.cloneBoard(ColorUtils.opponent(copyBoard.toMove), false);
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
		assertEquals(copyBoard.white_pawns, bitboardFromFieldName("a2"));
		assertEquals(copyBoard.white_knights, bitboardFromFieldName("b2"));
		assertEquals(copyBoard.white_bishops, bitboardFromFieldName("c2"));
		assertEquals(copyBoard.white_rooks, bitboardFromFieldName("d2"));
		assertEquals(copyBoard.white_queens, bitboardFromFieldName("e2"));
		assertEquals(copyBoard.white_kings, bitboardFromFieldName("f2"));

		assertEquals(copyBoard.black_pawns, bitboardFromFieldName("a7"));
		assertEquals(copyBoard.black_knights, bitboardFromFieldName("b7"));
		assertEquals(copyBoard.black_bishops, bitboardFromFieldName("c7"));
		assertEquals(copyBoard.black_rooks, bitboardFromFieldName("d7"));
		assertEquals(copyBoard.black_queens, bitboardFromFieldName("e7"));
		assertEquals(copyBoard.black_kings, bitboardFromFieldName("f7"));

		assertEquals(copyBoard.enemy_and_empty_board, bitboardFromBoard(
				"♟♟♟♟♟♟♟♟\n" +
				"......♟♟\n" +
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
				"♟♟♟♟♟♟..\n" +
				"........\n"));

		assertEquals(copyBoard.occupiedSquares[ColorUtils.BLACK], bitboardFromBoard(
				"........\n" +
				"♟♟♟♟♟♟..\n" +
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


		UnapplyableMove move = UnapplyableMoveUtils.toMove("f7e8", oldBoard);

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
