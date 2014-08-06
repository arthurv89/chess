package nl.arthurvlug.chess.engine;

import static nl.arthurvlug.chess.domain.board.InitializedBoard.c;
import static org.junit.Assert.assertEquals;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.board.InitializedBoard;
import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.engine.customEngine.ace.SimplePieceEvaluator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SimpleEvaluatorTest {
	private SimplePieceEvaluator evaluator;

	@Before
	public void before() {
		evaluator = new SimplePieceEvaluator();
	}
	
	@Test
	public void testEqual() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(0, score.getValue().intValue());
	}
	
	@Test
	public void testPlusPawn() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.PAWN, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(100, score.getValue().intValue());
	}
	
	@Test
	public void testMinusPawn() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.PAWN, Color.BLACK))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(-100, score.getValue().intValue());
	}
	
	@Test
	public void testPlusKnight() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.KNIGHT, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(280, score.getValue().intValue());
	}
	
	@Test
	public void testPlusBishop() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.BISHOP, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(290, score.getValue().intValue());
	}
	
	@Test
	public void testPlusRook() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.ROOK, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(500, score.getValue().intValue());
	}
	
	@Test
	public void testPlusQueen() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 0), new ColoredPiece(PieceType.KING, Color.WHITE))
					.put(c(0, 1), new ColoredPiece(PieceType.QUEEN, Color.WHITE))
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(975, score.getValue().intValue());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testIllegalBoard() {
		InitializedBoard board = new InitializedBoard(
				ImmutableMap.<Coordinates, ColoredPiece> builder()
					.put(c(0, 7), new ColoredPiece(PieceType.KING, Color.BLACK))
					.build()
		);
		evaluator.evaluate(board); // Throws exception
		throw new RuntimeException();
	}
}
