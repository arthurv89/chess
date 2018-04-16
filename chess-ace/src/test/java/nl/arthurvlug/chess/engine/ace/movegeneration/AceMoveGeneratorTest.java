package nl.arthurvlug.chess.engine.ace.movegeneration;

import com.google.common.collect.ImmutableList;
import java.util.List;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;
import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.castlingMoves;
import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.generateMoves;
import static nl.arthurvlug.chess.utils.board.FieldUtils.fieldIdx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AceMoveGeneratorTest {
	@Before
	public void before() {
		AceConfiguration.DEBUG = true;
	}

	@Test
	public void testKingMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("b7"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, fieldIdx("a7"));
		engineBoard.finalizeBitboards();

		List<Integer> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a2", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b2", engineBoard)));

		List<Integer> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a7a5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a7a6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7b8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7b6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c6", engineBoard)));
	}

	@Test
	public void testCastlingMovesWhite() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("h1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.finalizeBitboards();

		List<Integer> moves = castlingMoves(engineBoard);
		assertEquals(2, moves.size());
	}

	@Test
	public void testCastlingMovesWhite_rook_kingside_moved() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("h1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("h1h8", "e8e7", "h8h1", "e7e8"));

		List<Integer> moves = castlingMoves(engineBoard);
		assertEquals(1, moves.size());
	}

	@Test
	public void testCastling_when_white_castled_kingside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("h1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e1g1"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("e1"));
		assertEquals(WHITE_ROOK_BYTE, engineBoard.coloredPiece("f1"));
		assertEquals(WHITE_KING_BYTE, engineBoard.coloredPiece("g1"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("h1"));
	}

	@Test
	public void testCastling_when_white_castled_queenside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("h1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e1c1"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("e1"));
		assertEquals(WHITE_KING_BYTE, engineBoard.coloredPiece("c1"));
		assertEquals(WHITE_ROOK_BYTE, engineBoard.coloredPiece("d1"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("a1"));
	}

	@Test
	public void testCastlingMovesBlack() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.BLACK, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("a8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		List<Integer> moves = castlingMoves(engineBoard);
		assertEquals(2, moves.size());
	}

	@Test
	public void testCastling_when_black_castled_kingside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.BLACK, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("a8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e8g8"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("e8"));
		assertEquals(BLACK_ROOK_BYTE, engineBoard.coloredPiece("f8"));
		assertEquals(BLACK_KING_BYTE, engineBoard.coloredPiece("g8"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("h8"));
	}

	@Test
	public void testCastling_when_black_castled_queenside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.BLACK, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("a8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e8c8"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("e8"));
		assertEquals(BLACK_KING_BYTE, engineBoard.coloredPiece("c8"));
		assertEquals(BLACK_ROOK_BYTE, engineBoard.coloredPiece("d8"));
		assertEquals(NO_PIECE, engineBoard.coloredPiece("a8"));
	}

	@Test
	public void testRookMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("h8"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("a4"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("h6"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("b2"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, fieldIdx("f1"));
		engineBoard.finalizeBitboards();

		List<Integer> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(11, whiteMoves.size());
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a4a5", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a2", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a3", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1c1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1d1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1e1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1f1", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)));
		// Illegal moves
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)));

		List<Integer> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(19, blackMoves.size());
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b1", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b3", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b4", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2a2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2c2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2e2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2f2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2g2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2h2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)));
		// Illegal moves
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)));
	}

	@Test
	public void testKnightMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("h8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("h6"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KNIGHT, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KNIGHT, fieldIdx("b2"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, fieldIdx("c4"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("a4"));
		engineBoard.finalizeBitboards();
		
		List<Integer> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(6, whiteMoves.size());
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a4a5", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b3", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1c2", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)));

		List<Integer> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("c4c3", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2a4", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d3", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d1", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)));
	}

	@Test
	public void testBishopMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.BISHOP, fieldIdx("d1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("c1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("c2"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("d2"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("e2"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.BISHOP, fieldIdx("b7"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, fieldIdx("c6"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("c5"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("h8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("h6"));
		engineBoard.finalizeBitboards();
		
		List<Integer> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(9, whiteMoves.size());
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("e2e3", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("e2e4", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("d2d3", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("d2d4", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("c2c3", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("c2c4", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)));
		assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)));

		List<Integer> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(8, blackMoves.size());
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)));
	}

	@Test
	public void testQueenMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("h6"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.QUEEN, fieldIdx("d1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("c1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("c2"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("d2"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, fieldIdx("e2"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("h8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.QUEEN, fieldIdx("a8"));
		engineBoard.finalizeBitboards();
		
//		List<Integer> whiteMoves = EngineTestUtils.AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE));
//		assertEquals(0, whiteMoves.size());
		
		List<Integer> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(23, blackMoves.size());
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a4", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a3", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a1", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8b8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8c8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8d8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8e8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8f8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8g8", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8b7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8c6", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8d5", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8e4", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8f3", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8g2", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8h1", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)));
		assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)));
	}


	// TODO: Write for pawn moves
}
