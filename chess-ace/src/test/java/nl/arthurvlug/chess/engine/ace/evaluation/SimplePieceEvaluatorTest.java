package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.BLACK;
import static nl.arthurvlug.chess.engine.ColorUtils.WHITE;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.junit.Assert.assertEquals;

public class SimplePieceEvaluatorTest {
	private SimplePieceEvaluator evaluator;
	@Before
	public void before() {
		AceConfiguration.DEBUG = true;
		evaluator = new SimplePieceEvaluator();
	}
	
	@Test
	public void testEqual() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board.cloneBoard());
		assertEquals(0, score);
	}
	
	private int idx(String fieldName) {
		return FieldUtils.fieldIdx(fieldName);
	}

	@Test
	public void testPlusPawn() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(WHITE, PAWN, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(100, score);
	}
	
	@Test
	public void testMinusPawn() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(BLACK, PAWN, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(-100, score);
	}
	
	@Test
	public void testPlusKnight() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(WHITE, KNIGHT, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(280, score);
	}
	
	@Test
	public void testPlusBishop() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(WHITE, BISHOP, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(290, score);
	}
	
	@Test
	public void testPlusRook() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(WHITE, ROOK, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(500, score);
	}
	
	@Test
	public void testPlusQueen() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, idx("a1"));
		board.addPiece(WHITE, QUEEN, idx("a2"));
		board.addPiece(BLACK, KING, idx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(975, score);
	}
}
