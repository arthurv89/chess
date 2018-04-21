package nl.arthurvlug.chess.engine.ace.evaluation;

import java.util.Arrays;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.utils.MoveUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AceEvaluatorTest {
	private AceEvaluator aceEvaluator;

	@Before
	public void before() {
		MoveUtils.DEBUG = true;
		this.aceEvaluator = new AceEvaluator();
	}
	
	@Test
	public void testStartingPosition() {
		InitialACEBoard board = InitialACEBoard.createInitialACEBoard();
		board.finalizeBitboards();
		int evaluation = aceEvaluator.evaluate(board);
		assertEquals(0, evaluation);
	}
	
	@Test
	public void testAfterE4() {
		InitialACEBoard board = InitialACEBoard.createInitialACEBoard();
		board.apply(Arrays.asList("e2e4"));
		int evaluation = aceEvaluator.evaluate(board);
		assertEquals(50, evaluation);
	}
}
