package nl.arthurvlug.chess.engine.ace.movegeneration;

import com.google.common.collect.ImmutableList;
import java.util.List;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils.toMove;
import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.castlingMoves;
import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.generateMoves;
import static nl.arthurvlug.chess.utils.board.FieldUtils.fieldIdx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AceMoveGeneratorTest {
	@Test
	public void testKingMoves() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, false);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("b7"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, fieldIdx("a7"));
		engineBoard.finalizeBitboards();

		List<UnapplyableMove> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(toMove("a1b1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1a2", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1b2", engineBoard)));

		List<UnapplyableMove> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(toMove("a7a5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a7a6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7a8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7a6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7b8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7b6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7c8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7c7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7c6", engineBoard)));
	}

	@Test
	public void testCastlingMovesWhite() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.WHITE, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("a1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, fieldIdx("h1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.finalizeBitboards();

		List<UnapplyableMove> moves = castlingMoves(engineBoard);
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

		List<UnapplyableMove> moves = castlingMoves(engineBoard);
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
		assertEquals(null, engineBoard.pieceAt("e1"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.WHITE), engineBoard.pieceAt("f1"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.WHITE), engineBoard.pieceAt("g1"));
		assertEquals(null, engineBoard.pieceAt("h1"));
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
		assertEquals(null, engineBoard.pieceAt("e1"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.WHITE), engineBoard.pieceAt("c1"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.WHITE), engineBoard.pieceAt("d1"));
		assertEquals(null, engineBoard.pieceAt("a1"));
	}

	@Test
	public void testCastlingMovesBlack() throws Exception {
		ACEBoard engineBoard = ACEBoard.emptyBoard(ColorUtils.BLACK, true);
		engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, fieldIdx("e1"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("a8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, fieldIdx("e8"));
		engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, fieldIdx("h8"));
		engineBoard.finalizeBitboards();

		List<UnapplyableMove> moves = castlingMoves(engineBoard);
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
		assertEquals(null, engineBoard.pieceAt("e8"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.BLACK), engineBoard.pieceAt("f8"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.BLACK), engineBoard.pieceAt("g8"));
		assertEquals(null, engineBoard.pieceAt("h8"));
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
		assertEquals(null, engineBoard.pieceAt("e8"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.BLACK), engineBoard.pieceAt("c8"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.BLACK), engineBoard.pieceAt("d8"));
		assertEquals(null, engineBoard.pieceAt("a8"));
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

		List<UnapplyableMove> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(11, whiteMoves.size());
		assertTrue(whiteMoves.contains(toMove("a4a5", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1a2", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1a3", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1b1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1c1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1d1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1e1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1f1", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8g8", engineBoard)));
		// Illegal moves
		assertTrue(whiteMoves.contains(toMove("h8g7", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8h7", engineBoard)));

		List<UnapplyableMove> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(19, blackMoves.size());
		assertTrue(blackMoves.contains(toMove("b2b1", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b3", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b4", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2b8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2a2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2c2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2d2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2e2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2f2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2g2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2h2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6h5", engineBoard)));
		// Illegal moves
		assertTrue(blackMoves.contains(toMove("h6h7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g7", engineBoard)));
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
		
		List<UnapplyableMove> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(6, whiteMoves.size());
		assertTrue(whiteMoves.contains(toMove("a4a5", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1b3", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("a1c2", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8g8", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8h7", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8g7", engineBoard)));

		List<UnapplyableMove> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(toMove("c4c3", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2a4", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2d3", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b2d1", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6h5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6h7", engineBoard)));
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
		
		List<UnapplyableMove> whiteMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false));
		assertEquals(9, whiteMoves.size());
		assertTrue(whiteMoves.contains(toMove("h8g8", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("e2e3", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("e2e4", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("d2d3", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("d2d4", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("c2c3", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("c2c4", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8h7", engineBoard)));
		assertTrue(whiteMoves.contains(toMove("h8g7", engineBoard)));

		List<UnapplyableMove> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(8, blackMoves.size());
		assertTrue(blackMoves.contains(toMove("b7a8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7c8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("b7a6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6h5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6h7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h6g7", engineBoard)));
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
		
//		List<UnapplyableMove> whiteMoves = EngineTestUtils.AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE));
//		assertEquals(0, whiteMoves.size());
		
		List<UnapplyableMove> blackMoves = generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false));
		assertEquals(23, blackMoves.size());
		assertTrue(blackMoves.contains(toMove("a8a7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a4", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a3", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8a1", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8b8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8c8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8d8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8e8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8f8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8g8", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8b7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8c6", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8d5", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8e4", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8f3", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8g2", engineBoard)));
		assertTrue(blackMoves.contains(toMove("a8h1", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h8h7", engineBoard)));
		assertTrue(blackMoves.contains(toMove("h8g7", engineBoard)));
	}


	// TODO: Write for pawn moves
}
