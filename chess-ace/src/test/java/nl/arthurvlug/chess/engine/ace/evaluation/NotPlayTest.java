package nl.arthurvlug.chess.engine.ace.evaluation;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;

import org.junit.Ignore;
import org.junit.Test;

public class NotPlayTest {
	private AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new AceEvaluator());

	@Ignore
	@Test
	public void testShouldNotPlayImmobilityMove() {
		AceMove move = think(splitToMoves("b1c3, g8f6, g1f3, d7d5, d2d4, b8c6"));
		assertFalse("c1e3".equals(move.toString()));
	}

	private AceMove think(List<String> moveList) {
		InitialEngineBoard board = new InitialEngineBoard();
		board.apply(moveList);
		AceMove move = algorithm.think(board, 4);
		System.out.println(move);
		return move;
	}

	private List<String> splitToMoves(String string) {
		return Arrays.asList(string.split(", "));
	}
}
