package nl.arthurvlug.chess.engine.ace.evaluation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;

import org.junit.Before;
import org.junit.Test;

public class AceEvaluatorTest {
	private AceEvaluator aceEvaluator;
	
	@Before
	public void before() {
		this.aceEvaluator = new AceEvaluator();
	}
	
	@Test
	public void testStartingPosition() {
		int evaluation = aceEvaluator.evaluate(new InitialEngineBoard()).getValue();
		assertEquals(0, evaluation);
	}
	
	@Test
	public void testAfterE4() {
		InitialEngineBoard board = new InitialEngineBoard();
		board.apply(Arrays.asList("e2e4"));
		int evaluation = aceEvaluator.evaluate(board).getValue();
		assertEquals(4, evaluation);
	}
}
