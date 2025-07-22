package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import java.util.List;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils.createMove;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromBoard;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.board.pieces.Color.BLACK;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

	@BeforeEach
	public void before() {
		MoveUtils.DEBUG = false;
	}

	@Test
	public void testUnapplyAfterCastlingPutsRookBack() throws Exception {
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
		final AlphaBetaPruningAlgorithm algorithm = createAlgorithm(configuration);
		final Move move = algorithm.startThinking(engineBoard).toBlocking().first();

		// Check that the rook moves back to h8 after considering castling king-side
		assertThat(move.toString()).isNotEqualTo("f8f4");
	}

	private AlphaBetaPruningAlgorithm createAlgorithm(final AceConfiguration configuration) {
		final AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(configuration);
		algorithm.setEventBus(new EventBus());
		return algorithm;
	}

	// Check ignored after Nd2
//	[10:04:59:059 BST]  INFO [CustomEngine 27] n.a.c.e.c.CustomEngine: [d2d4, d7d5, b1c3, b8c6, c1f4, c8f5, c3b5, a8c8, g1f3, e7e6, b5c7, c8c7, f4c7, d8c7, e2e3, c7b6, d1c1, c6b4, f1d3, f5d3, e1d2, d3e4, c2c4, b4c2, a1b1, f8b4, d2d1, c2e3, f2e3, e4b1, c4c5, b6a5, c1b1, g8f6, a2a3, a5a4, d1c1, b4a5, b1d3, f6e4, h1f1, a5c7, c1b1, e8f8, d3d1, a4b5, b1a1, f8g8, a3a4, b5c6, a1b1, b7b6, c5b6, c6b6, d1c1, b6b3, f3e5, f7f5, e5f7, g8f7, c1c7, f7g6, f1c1, e4d2, c7g3]


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


		Integer move = createMove("f7e8", oldBoard);

		final ACEBoard newBoard = startPositionBoard.cloneBoard();
		boolean white_king_or_rook_queen_side_moved = newBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = newBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = newBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = newBoard.black_king_or_rook_king_side_moved;
		newBoard.apply(move);
		newBoard.unapply(move, white_king_or_rook_queen_side_moved, white_king_or_rook_king_side_moved, black_king_or_rook_queen_side_moved, black_king_or_rook_king_side_moved, 0);
		assertEquals(ACEBoardUtils.stringDump(oldBoard), ACEBoardUtils.stringDump(newBoard));
	}

	@Test
	public void testPiecesArray() throws KingEatingException {
		final ACEBoard board = ACEBoardUtils.initializedBoard(WHITE, "" +
				"♟♞♝♜♛♚..\n" +
				".....♙..\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"........\n" +
				"♙♘♗♖♕♔..\n");
		final ACEBoard oldBoard = board.cloneBoard();

		final List<Integer> takeMoves = board.generateTakeMoves();
		assertThat(takeMoves).containsExactlyInAnyOrder(
				createMove("f7e8q", board),
				createMove("f7e8r", board),
				createMove("f7e8l", board),
				createMove("f7e8n", board),
				createMove("e1e8", board),
				createMove("d1d8", board)
		);

		int move = createMove("d1d8", board);
		board.apply(move);
		board.unapply(move, true, true, true, true, 0);

		assertThat(ACEBoardUtils.stringDump(board)).isEqualTo(ACEBoardUtils.stringDump(oldBoard));
	}

	@Test
	public void testUnapplyTakeMove() throws Exception {
		verifyBitboards(startPositionBoard);
	}

	@Test
	public void testPromotion() {
		final ACEBoard board = ACEBoardUtils.initializedBoard(BLACK, "" +
				"......♚.\n" +
				"......♟♟\n" +
				".♙......\n" +
				"....♟...\n" +
				".♙......\n" +
				"......♜.\n" +
				"♔....♟..\n" +
				"........");
		final ACEBoard clonedBoard = board.cloneBoard();
		int move = createMove("f2f1q", board);
		board.apply(move);

		assertThat(board.string()).isEqualTo("" +
				"......♚.\n" +
				"......♟♟\n" +
				".♙......\n" +
				"....♟...\n" +
				".♙......\n" +
				"......♜.\n" +
				"♔.......\n" +
				".....♛..\n");

		board.unapply(move, true, true, true, true, 0);
		assertThat(ACEBoardUtils.stringDump(clonedBoard)).isEqualTo(ACEBoardUtils.stringDump(board));
	}

}
