package nl.arthurvlug.chess.engine.ace.board;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.PieceUtils;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.utils.AceBoardTestUtils;
import nl.arthurvlug.chess.utils.jackson.JacksonUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils.createMove;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.initializedBoard;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.stringDump;
import static nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove.create;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromBoard;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.getUnapplyFlags;
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
		MoveUtils.DEBUG = true;
	}

	@Test
	public void testUnapplyAfterCastlingPutsRookBack() {
		ACEBoard engineBoard = initializedBoard(BLACK, """
				....♚..♜
				♟.....♟♟
				.♝..♟...
				...♟....
				♛..♙♞♙..
				♙..♕.♘..
				.♙....♙♙
				.♔...♖..
				""");
		UnapplyFlags unapplyFlags = getUnapplyFlags(engineBoard);
		String dumpBefore = stringDump(engineBoard);
		int move = createMove("e8g8", engineBoard);
		engineBoard.apply(move);
		AceBoardTestUtils.unapply(engineBoard, move, unapplyFlags);
		String dumpAfter = stringDump(engineBoard);

		// Check that the rook moves back to h8 after considering castling king-side
		assertThat(dumpBefore).isEqualTo(dumpAfter);
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
//		boolean incFiftyClock = newBoard.incFiftyClock;
		newBoard.apply(move);
		newBoard.unapply(move, white_king_or_rook_queen_side_moved, white_king_or_rook_king_side_moved, black_king_or_rook_queen_side_moved, black_king_or_rook_king_side_moved);
		assertEquals(stringDump(oldBoard), stringDump(newBoard));
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
				createMove("f7e8b", board),
				createMove("f7e8n", board),
				createMove("e1e8", board),
				createMove("d1d8", board)
		);

		int move = createMove("d1d8", board);
		board.apply(move);

		board.unapply(move, true, true, true, true);

		assertThat(stringDump(board)).isEqualTo(stringDump(oldBoard));
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
//		boolean incFiftyClock = board.incFiftyClock;

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

		board.unapply(move, true, true, true, true);
		assertThat(stringDump(clonedBoard)).isEqualTo(stringDump(board));
	}

	@Test
	public void applyF4G3() {
		String expectedInitialBoard = """
				..♝....♜
				♟.♘.♟♟♝♟
				.♟..♚.♟.
				.♗.♟♙♞..
				.♙...♗..
				♛.♙.♙...
				♙....♙♙♙
				♖..♕♔..♖
				""";
		String expectedBoardAfterMoving = """
				..♝....♜
				♟.♘.♟♟♝♟
				.♟..♚.♟.
				.♗.♟♙♞..
				.♙......
				♛.♙.♙.♗.
				♙....♙♙♙
				♖..♕♔..♖
				""";
		String initialEngineBoardJson = """
				{
				  "toMove" : 0,
				  "black_kings" : 17592186044416,
				  "white_kings" : 16,
				  "black_queens" : 65536,
				  "white_queens" : 8,
				  "white_rooks" : 129,
				  "black_rooks" : -9223372036854775808,
				  "white_bishops" : 9126805504,
				  "black_bishops" : 306244774661193728,
				  "white_knights" : 1125899906842624,
				  "black_knights" : 137438953472,
				  "white_pawns" : 68754399488,
				  "black_pawns" : 49893673004957696,
				  "occupiedSquares" : [ 1125977788047769, -8867215859563560960 ],
				  "unoccupied_board" : 8866089881775513190,
				  "occupied_board" : -8866089881775513191,
				  "enemy_and_empty_board" : -1125977788047770,
				  "white_king_or_rook_queen_side_moved" : false,
				  "white_king_or_rook_king_side_moved" : false,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : false,
				  "pieces" : [ 4, 0, 0, 5, 6, 0, 0, 4, 1, 0, 0, 0, 0, 1, 1, 1, 11, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 3, 0, 7, 1, 8, 0, 0, 0, 7, 0, 0, 12, 0, 7, 0, 7, 0, 2, 0, 7, 7, 9, 7, 0, 0, 9, 0, 0, 0, 0, 10 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -513536744,
				  "plyStack" : [ 198045 ]
				}""";

		String expectedEngineBoardAfterMove = """
				{
				  "toMove" : 1,
				  "black_kings" : 17592186044416,
				  "white_kings" : 16,
				  "black_queens" : 65536,
				  "white_queens" : 8,
				  "white_rooks" : 129,
				  "black_rooks" : -9223372036854775808,
				  "white_bishops" : 8594128896,
				  "black_bishops" : 306244774661193728,
				  "white_knights" : 1125899906842624,
				  "black_knights" : 137438953472,
				  "white_pawns" : 68754399488,
				  "black_pawns" : 49893673004957696,
				  "occupiedSquares" : [ 1125977255371161, -8867215859563560960 ],
				  "unoccupied_board" : 8866089882308189798,
				  "occupied_board" : -8866089882308189799,
				  "enemy_and_empty_board" : 8867215859563560959,
				  "white_king_or_rook_queen_side_moved" : false,
				  "white_king_or_rook_king_side_moved" : false,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : false,
				  "pieces" : [ 4, 0, 0, 5, 6, 0, 0, 4, 1, 0, 0, 0, 0, 1, 1, 1, 11, 0, 1, 0, 1, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 3, 0, 7, 1, 8, 0, 0, 0, 7, 0, 0, 12, 0, 7, 0, 7, 0, 2, 0, 7, 7, 9, 7, 0, 0, 9, 0, 0, 0, 0, 10 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -607014438,
				  "plyStack" : [ 198045, 198045 ]
				}""";

		ACEBoard engineBoard = JacksonUtils.fromJson(initialEngineBoardJson, new TypeReference<>() {});
		assertThat(engineBoard.string()).isEqualTo(expectedInitialBoard);
		assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(initialEngineBoardJson);

		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
//		int fiftyMove = engineBoard.getFiftyMove();
//		boolean incFiftyClock = engineBoard.incFiftyClock;

		String engineBoardStringBefore = engineBoard.string();

		int move = createMoveForSquares(engineBoard);
		engineBoard.apply(move);

		assertThat(engineBoard.string()).isEqualTo(expectedBoardAfterMoving);

		assertThat(engineBoardStringBefore).isNotEqualTo(expectedEngineBoardAfterMove);
		assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(expectedEngineBoardAfterMove);

		engineBoard.unapply(move,
				white_king_or_rook_queen_side_moved,
				white_king_or_rook_king_side_moved,
				black_king_or_rook_queen_side_moved,
				black_king_or_rook_king_side_moved);

		assertThat(engineBoard.string()).isEqualTo(engineBoardStringBefore);
		assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(initialEngineBoardJson);
	}

	private static int createMoveForSquares(ACEBoard engineBoard) {
		String fromField = "f4";
		String toField = "g3";
		byte fromIdx = (byte) Long.numberOfTrailingZeros(bitboardFromFieldName(fromField));
		byte targetIdx = (byte) Long.numberOfTrailingZeros(bitboardFromFieldName(toField));
		int coloredMovingPiece = engineBoard.coloredPiece(fromField);
		int move = create(
				fromIdx,
				targetIdx,
				coloredMovingPiece,
				NO_PIECE,
				NO_PIECE
		);
		return move;
	}

	private static String printPieces(ACEBoard engineBoard) {
		byte[] array = engineBoard.getPieces();
		int chunkSize = 8;

		List<String> chunks = new ArrayList<>();
		for (int i = 0; i < array.length; i += chunkSize) {
			StringBuilder sb = new StringBuilder();
			for (int j = i; j < Math.min(i + chunkSize, array.length); j++) {
				char piece = PieceUtils.typeOrDot(array[j]);
				Optional<ColoredPiece> coloredPieceOpt = PieceStringUtils.coloredPieceFromCharacter(piece, PieceStringUtils.pieceToCharacterConverter);
				if(coloredPieceOpt.isPresent()) {
					ColoredPiece coloredPiece = coloredPieceOpt.get();
					PieceSymbol pieceSymbol = PieceStringUtils.pieceToChessSymbolMap.getMap().get(coloredPiece.getPieceType());
					sb.append(coloredPiece.getColor() == WHITE ? pieceSymbol.getWhite() : pieceSymbol.getBlack());
				} else {
					sb.append(' ');
				}
			}
			chunks.add("\n%s".formatted(sb));
		}
		Collections.reverse(chunks);
		return chunks.toString();
	}

	@Test
	public void testQueenTakesQueen() {
		String position = """
				♚♜......
				.♟.♟....
				♟.♟.....
				.♟......
				........
				........
				♖.......
				♔.......
				""";
		String json = """
				{
				  "toMove" : 0,
				  "black_kings" : 72057594037927936,
				  "white_kings" : 1,
				  "black_queens" : 0,
				  "white_queens" : 0,
				  "white_rooks" : 256,
				  "black_rooks" : 144115188075855872,
				  "white_bishops" : 0,
				  "black_bishops" : 0,
				  "white_knights" : 0,
				  "black_knights" : 0,
				  "white_pawns" : 0,
				  "black_pawns" : 2820255915180032,
				  "occupiedSquares" : [ 257, 218993038028963840 ],
				  "unoccupied_board" : -218993038028964098,
				  "occupied_board" : 218993038028964097,
				  "enemy_and_empty_board" : -258,
				  "white_king_or_rook_queen_side_moved" : true,
				  "white_king_or_rook_king_side_moved" : true,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : true,
				  "pieces" : [ 6, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 7, 0, 0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 12, 10, 0, 0, 0, 0, 0, 0 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -678140938,
				  "plyStack" : [ ]
				}""";
		ACEBoard aceBoard = JacksonUtils.fromJson(json, new TypeReference<>() {});

		String json2 = JacksonUtils.toJson(aceBoard);
		assertThat(json2).isEqualTo(json);

		assertThat(aceBoard.string()).isEqualTo(position);

//		aceBoard.checkConsistency();

		int move = UnapplyableMoveUtils.createMove("b5f1", aceBoard);
		assertThat(move).isEqualTo(459105);

		MoveUtils.DEBUG = false;
		boolean white_king_or_rook_queen_side_moved = aceBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = aceBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = aceBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = aceBoard.black_king_or_rook_king_side_moved;

		String expected = stringDump(aceBoard);

		aceBoard.apply(move);
		aceBoard.unapply(move,
				white_king_or_rook_queen_side_moved,
				white_king_or_rook_king_side_moved,
				black_king_or_rook_queen_side_moved,
				black_king_or_rook_king_side_moved);

		String actual = stringDump(aceBoard);
		assertThat(actual).isEqualTo(expected);
	}
}
