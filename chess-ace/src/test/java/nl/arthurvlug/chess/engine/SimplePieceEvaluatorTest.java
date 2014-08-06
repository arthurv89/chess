package nl.arthurvlug.chess.engine;

import static org.junit.Assert.assertEquals;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.engine.customEngine.EngineBoard;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.engine.customEngine.ace.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;

import org.junit.Before;
import org.junit.Test;

public class SimplePieceEvaluatorTest {
	private SimplePieceEvaluator evaluator;

	@Before
	public void before() {
		evaluator = new SimplePieceEvaluator();
	}
	
	@Test
	public void testEqual() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(new EngineBoard(board));
		assertEquals(0, score.getValue().intValue());
	}
	
	private int idx(String fieldName) {
		return BitboardUtils.toIndex(fieldName);
	}

	@Test
	public void testPlusPawn() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.PAWN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(100, score.getValue().intValue());
	}
	
	@Test
	public void testMinusPawn() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.BLACK, PieceType.PAWN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(-100, score.getValue().intValue());
	}
	
	@Test
	public void testPlusKnight() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(280, score.getValue().intValue());
	}
	
	@Test
	public void testPlusBishop() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.BISHOP, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(290, score.getValue().intValue());
	}
	
	@Test
	public void testPlusRook() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.ROOK, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(500, score.getValue().intValue());
	}
	
	@Test
	public void testPlusQueen() {
		EngineBoard board = new EngineBoard(EngineConstants.WHITE);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.QUEEN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finished();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(975, score.getValue().intValue());
	}
}
