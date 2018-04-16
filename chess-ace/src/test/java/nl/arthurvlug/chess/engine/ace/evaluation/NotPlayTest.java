package nl.arthurvlug.chess.engine.ace.evaluation;

import java.util.Arrays;
import java.util.List;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.utils.game.Move;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class NotPlayTest {
	private AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new AceConfiguration());

	@Before
	public void before() {
		AceConfiguration.DEBUG = true;
	}

	@Ignore
	@Test
	public void testShouldNotPlayImmobilityMove() {
		Move move = think(splitToMoves("b1c3, g8f6, g1f3, d7d5, d2d4, b8c6"));
		assertFalse("c1e3".equals(move.toString()));
	}

	private Move think(List<String> moveList) {
		InitialACEBoard board = InitialACEBoard.createInitialACEBoard();
		board.apply(moveList);
		Move move = algorithm.think(board);
		System.out.println(move);
		return move;
	}

	private List<String> splitToMoves(String string) {
		return Arrays.asList(string.split(", "));
	}
}
