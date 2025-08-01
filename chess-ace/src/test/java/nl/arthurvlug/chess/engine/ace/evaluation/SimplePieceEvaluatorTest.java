package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.MoveUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.BLACK;
import static nl.arthurvlug.chess.engine.ColorUtils.WHITE;
import static nl.arthurvlug.chess.utils.board.FieldUtils.fieldIdx;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePieceEvaluatorTest {
	private SimplePieceEvaluator evaluator;
	@BeforeEach
	public void before() {
		evaluator = new SimplePieceEvaluator();
	}
	
	@Test
	public void testEqual() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board.cloneBoard());
		assertEquals(0, score);
	}
	
	@Test
	public void testPlusPawn() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(WHITE, PAWN, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(100, score);
	}
	
	@Test
	public void testMinusPawn() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(BLACK, PAWN, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(-100, score);
	}
	
	@Test
	public void testPlusKnight() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(WHITE, KNIGHT, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(213, score);
	}
	
	@Test
	public void testPlusBishop() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(WHITE, BISHOP, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(243, score);
	}
	
	@Test
	public void testPlusRook() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(WHITE, ROOK, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(352, score);
	}
	
	@Test
	public void testPlusQueen() {
		ACEBoard board = ACEBoard.emptyBoard(WHITE, false);
		board.addPiece(WHITE, KING, fieldIdx("a1"));
		board.addPiece(WHITE, QUEEN, fieldIdx("a2"));
		board.addPiece(BLACK, KING, fieldIdx("a8"));
		board.finalizeBitboards();
		
		int score = evaluator.evaluate(board);
		assertEquals(786, score);
	}
}
