package nl.arthurvlug.chess.engine.ace.movegeneration;

import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.*;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.inject.matcher.Matchers;
import java.util.List;

import java.util.Optional;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Test;

public class AceMoveGeneratorTest {
	@Test
	public void testKingMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, false);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, toIndex("a7"));
		engineBoard.finalizeBitboards();

		List<Move> whiteMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE, false));
		assertEquals(3, whiteMoves.size());
		assertTrue(whiteMoves.contains(move("a1b1")));
		assertTrue(whiteMoves.contains(move("a1a2")));
		assertTrue(whiteMoves.contains(move("a1b2")));

		List<Move> blackMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(move("a7a5")));
		assertTrue(blackMoves.contains(move("a7a6")));
		assertTrue(blackMoves.contains(move("b7a8")));
		assertTrue(blackMoves.contains(move("b7a6")));
		assertTrue(blackMoves.contains(move("b7b8")));
		assertTrue(blackMoves.contains(move("b7b6")));
		assertTrue(blackMoves.contains(move("b7c8")));
		assertTrue(blackMoves.contains(move("b7c7")));
		assertTrue(blackMoves.contains(move("b7c6")));
	}

	@Test
	public void testCastlingMovesWhite() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("h1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.finalizeBitboards();

		List<Move> moves = castlingMoves(engineBoard);
		assertEquals(2, moves.size());
	}

	@Test
	public void testCastlingMovesWhite_rook_kingside_moved() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("h1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("h1h8", "e8e7", "h8h1", "e7e8"));

		List<Move> moves = castlingMoves(engineBoard);
		assertEquals(1, moves.size());
	}

	@Test
	public void testCastling_when_white_castled_kingside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("h1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e1g1"));
		assertEquals(null, engineBoard.pieceAt("e1"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.WHITE), engineBoard.pieceAt("f1"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.WHITE), engineBoard.pieceAt("g1"));
		assertEquals(null, engineBoard.pieceAt("h1"));
	}

	@Test
	public void testCastling_when_white_castled_queenside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("h1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e1c1"));
		assertEquals(null, engineBoard.pieceAt("e1"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.WHITE), engineBoard.pieceAt("c1"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.WHITE), engineBoard.pieceAt("d1"));
		assertEquals(null, engineBoard.pieceAt("a1"));
	}

	@Test
	public void testCastlingMovesBlack() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.BLACK, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("h8"));
		engineBoard.finalizeBitboards();

		List<Move> moves = castlingMoves(engineBoard);
		assertEquals(2, moves.size());
	}

	@Test
	public void testCastling_when_black_castled_kingside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.BLACK, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("h8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e8g8"));
		assertEquals(null, engineBoard.pieceAt("e8"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.BLACK), engineBoard.pieceAt("f8"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.BLACK), engineBoard.pieceAt("g8"));
		assertEquals(null, engineBoard.pieceAt("h8"));
	}

	@Test
	public void testCastling_when_black_castled_queenside_then_verify_king_and_rook_position() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.BLACK, true);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("h8"));
		engineBoard.finalizeBitboards();

		engineBoard.apply(ImmutableList.of("e8c8"));
		assertEquals(null, engineBoard.pieceAt("e8"));
		assertEquals(new ColoredPiece(PieceType.KING, Color.BLACK), engineBoard.pieceAt("c8"));
		assertEquals(new ColoredPiece(PieceType.ROOK, Color.BLACK), engineBoard.pieceAt("d8"));
		assertEquals(null, engineBoard.pieceAt("a8"));
	}

	@Test
	public void testRookMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, false);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("h8"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("a4"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("h6"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, toIndex("f1"));
		engineBoard.finalizeBitboards();

		List<Move> whiteMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE, false));
		assertEquals(11, whiteMoves.size());
		assertTrue(whiteMoves.contains(move("a4a5")));
		assertTrue(whiteMoves.contains(move("a1a2")));
		assertTrue(whiteMoves.contains(move("a1a3")));
		assertTrue(whiteMoves.contains(move("a1b1")));
		assertTrue(whiteMoves.contains(move("a1c1")));
		assertTrue(whiteMoves.contains(move("a1d1")));
		assertTrue(whiteMoves.contains(move("a1e1")));
		assertTrue(whiteMoves.contains(move("a1f1")));
		assertTrue(whiteMoves.contains(move("h8g8")));
		// Illegal moves
		assertTrue(whiteMoves.contains(move("h8g7")));
		assertTrue(whiteMoves.contains(move("h8h7")));

		List<Move> blackMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK, false));
		assertEquals(19, blackMoves.size());
		assertTrue(blackMoves.contains(move("b2b1")));
		assertTrue(blackMoves.contains(move("b2b3")));
		assertTrue(blackMoves.contains(move("b2b4")));
		assertTrue(blackMoves.contains(move("b2b5")));
		assertTrue(blackMoves.contains(move("b2b6")));
		assertTrue(blackMoves.contains(move("b2b7")));
		assertTrue(blackMoves.contains(move("b2b8")));
		assertTrue(blackMoves.contains(move("b2a2")));
		assertTrue(blackMoves.contains(move("b2c2")));
		assertTrue(blackMoves.contains(move("b2d2")));
		assertTrue(blackMoves.contains(move("b2e2")));
		assertTrue(blackMoves.contains(move("b2f2")));
		assertTrue(blackMoves.contains(move("b2g2")));
		assertTrue(blackMoves.contains(move("b2h2")));
		assertTrue(blackMoves.contains(move("h6g6")));
		assertTrue(blackMoves.contains(move("h6g5")));
		assertTrue(blackMoves.contains(move("h6h5")));
		// Illegal moves
		assertTrue(blackMoves.contains(move("h6h7")));
		assertTrue(blackMoves.contains(move("h6g7")));
	}

	@Test
	public void testKnightMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, false);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("h8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("h6"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, toIndex("a1"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, toIndex("b2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, toIndex("c4"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("a4"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE, false));
		assertEquals(6, whiteMoves.size());
		assertTrue(whiteMoves.contains(move("a4a5")));
		assertTrue(whiteMoves.contains(move("a1b3")));
		assertTrue(whiteMoves.contains(move("a1c2")));
		assertTrue(whiteMoves.contains(move("h8g8")));
		assertTrue(whiteMoves.contains(move("h8h7")));
		assertTrue(whiteMoves.contains(move("h8g7")));

		List<Move> blackMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK, false));
		assertEquals(9, blackMoves.size());
		assertTrue(blackMoves.contains(move("c4c3")));
		assertTrue(blackMoves.contains(move("b2a4")));
		assertTrue(blackMoves.contains(move("b2d3")));
		assertTrue(blackMoves.contains(move("b2d1")));
		assertTrue(blackMoves.contains(move("h6g6")));
		assertTrue(blackMoves.contains(move("h6g5")));
		assertTrue(blackMoves.contains(move("h6h5")));
		assertTrue(blackMoves.contains(move("h6g7")));
		assertTrue(blackMoves.contains(move("h6h7")));
	}

	@Test
	public void testBishopMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, false);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("e2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, toIndex("c6"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("c5"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("h8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("h6"));
		engineBoard.finalizeBitboards();
		
		List<Move> whiteMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE, false));
		assertEquals(9, whiteMoves.size());
		assertTrue(whiteMoves.contains(move("h8g8")));
		assertTrue(whiteMoves.contains(move("e2e3")));
		assertTrue(whiteMoves.contains(move("e2e4")));
		assertTrue(whiteMoves.contains(move("d2d3")));
		assertTrue(whiteMoves.contains(move("d2d4")));
		assertTrue(whiteMoves.contains(move("c2c3")));
		assertTrue(whiteMoves.contains(move("c2c4")));
		assertTrue(whiteMoves.contains(move("h8h7")));
		assertTrue(whiteMoves.contains(move("h8g7")));

		List<Move> blackMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK, false));
		assertEquals(8, blackMoves.size());
		assertTrue(blackMoves.contains(move("b7a8")));
		assertTrue(blackMoves.contains(move("b7c8")));
		assertTrue(blackMoves.contains(move("b7a6")));
		assertTrue(blackMoves.contains(move("h6g6")));
		assertTrue(blackMoves.contains(move("h6g5")));
		assertTrue(blackMoves.contains(move("h6h5")));
		assertTrue(blackMoves.contains(move("h6h7")));
		assertTrue(blackMoves.contains(move("h6g7")));
	}

	@Test
	public void testQueenMoves() throws Exception {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, false);
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, toIndex("h6"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, toIndex("e2"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, toIndex("h8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, toIndex("a8"));
		engineBoard.finalizeBitboards();
		
//		List<Move> whiteMoves = EngineTestUtils.AceMoveGenerator.generateMoves(new ACEBoard(engineBoard, EngineConstants.WHITE));
//		assertEquals(0, whiteMoves.size());
		
		List<Move> blackMoves = generateMoves(new ACEBoard(engineBoard, EngineConstants.BLACK, false));
		assertEquals(23, blackMoves.size());
		assertTrue(blackMoves.contains(move("a8a7")));
		assertTrue(blackMoves.contains(move("a8a6")));
		assertTrue(blackMoves.contains(move("a8a5")));
		assertTrue(blackMoves.contains(move("a8a4")));
		assertTrue(blackMoves.contains(move("a8a3")));
		assertTrue(blackMoves.contains(move("a8a2")));
		assertTrue(blackMoves.contains(move("a8a1")));
		assertTrue(blackMoves.contains(move("a8b8")));
		assertTrue(blackMoves.contains(move("a8c8")));
		assertTrue(blackMoves.contains(move("a8d8")));
		assertTrue(blackMoves.contains(move("a8e8")));
		assertTrue(blackMoves.contains(move("a8f8")));
		assertTrue(blackMoves.contains(move("a8g8")));
		assertTrue(blackMoves.contains(move("a8b7")));
		assertTrue(blackMoves.contains(move("a8c6")));
		assertTrue(blackMoves.contains(move("a8d5")));
		assertTrue(blackMoves.contains(move("a8e4")));
		assertTrue(blackMoves.contains(move("a8f3")));
		assertTrue(blackMoves.contains(move("a8g2")));
		assertTrue(blackMoves.contains(move("a8h1")));
		assertTrue(blackMoves.contains(move("h8h7")));
		assertTrue(blackMoves.contains(move("h8g7")));
	}


	// TODO: Write for pawn moves
}
