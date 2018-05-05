package nl.arthurvlug.chess.engine.ace.evaluation;

import com.google.common.collect.ImmutableList;
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
		MoveUtils.DEBUG = false;
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

	@Test
	public void testRepetition() {
		InitialACEBoard board = InitialACEBoard.createInitialACEBoard();
		board.apply(ImmutableList.of("b1d8", "b8c6", "d8b1", "c6b8", "b1d8", "b8c6", "d8b1", "c6b8", "b1d8"));
		int evaluation = aceEvaluator.evaluate(board);
		assertEquals(0, evaluation);
	}

	@Test
	public void testRepetition2() {
		InitialACEBoard board = InitialACEBoard.createInitialACEBoard();
		board.apply(ImmutableList.of("d2d4", "d7d5", "b1c3", "b8c6", "g1f3", "g8f6", "e2e3", "c8f5", "c1d2", "e7e6", "f1d3", "f5d3", "c2d3", "f8d6", "d1b3", "a8b8", "e1g1", "e8g8", "e3e4", "c6b4", "e4e5", "b4d3", "e5d6", "d8d6", "c3b5", "d3c5", "b3c2", "d6e7", "c2c5", "e7c5", "d4c5", "c7c6", "b5d4", "f6e4", "d2f4", "b8a8", "a1c1", "a8c8", "f3e5", "c8a8", "c1c2", "a8c8", "f1a1", "c8a8", "a1b1", "a8c8", "b1a1", "c8a8", "a1b1", "a8c8", "b1a1", "c8a8", "a1b1", "a8c8", "b1a1"));
		int evaluation = aceEvaluator.evaluate(board);
		assertEquals(0, evaluation);
	}
}
