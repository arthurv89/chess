package nl.arthurvlug.chess.engine.performance;

import java.util.Arrays;

import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;

public class AceEvaluatorPerformanceTest {
	public static void main(String[] args) {
		InitialEngineBoard board = new InitialEngineBoard();
		board.apply(Arrays.asList("e2e4"));
		PerformanceRunner.run(board, new AceEvaluator());
	}
}
