package nl.arthurvlug.chess.engine.ace.evaluation;

import static org.junit.Assert.assertEquals;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

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
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(new ACEBoard(board));
		assertEquals(0, score.getValue().intValue());
	}
	
	private int idx(String fieldName) {
		return BitboardUtils.toIndex(fieldName);
	}

	@Test
	public void testPlusPawn() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.PAWN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(100, score.getValue().intValue());
	}
	
	@Test
	public void testMinusPawn() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.BLACK, PieceType.PAWN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(-100, score.getValue().intValue());
	}
	
	@Test
	public void testPlusKnight() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(280, score.getValue().intValue());
	}
	
	@Test
	public void testPlusBishop() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.BISHOP, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(290, score.getValue().intValue());
	}
	
	@Test
	public void testPlusRook() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.ROOK, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(500, score.getValue().intValue());
	}
	
	@Test
	public void testPlusQueen() {
		ACEBoard board = new ACEBoard(EngineConstants.WHITE, false);
		board.addPiece(EngineConstants.WHITE, PieceType.KING, idx("a1"));
		board.addPiece(EngineConstants.WHITE, PieceType.QUEEN, idx("a2"));
		board.addPiece(EngineConstants.BLACK, PieceType.KING, idx("a8"));
		board.finalizeBitboards();
		
		NormalScore score = (NormalScore) evaluator.evaluate(board);
		assertEquals(975, score.getValue().intValue());
	}
}
