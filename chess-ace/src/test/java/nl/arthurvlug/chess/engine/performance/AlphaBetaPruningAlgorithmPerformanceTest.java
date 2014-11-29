package nl.arthurvlug.chess.engine.performance;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;

import org.joda.time.DateTime;

@Slf4j
public class AlphaBetaPruningAlgorithmPerformanceTest {
	public static void main(String[] args) {
		performanceTest(1);
		performanceTest(2);
		performanceTest(3);
		performanceTest(4);
		performanceTest(5);
		performanceTest(6);
	}

	private static void performanceTest(int depth) {
		log.debug("Performance test for depth {}", depth);
		final InitialEngineBoard board = new InitialEngineBoard();
		final AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new AceEvaluator());
		
		final DateTime start = DateTime.now();
		algorithm.think(board, depth);
		final DateTime end = DateTime.now();
		
		final int nodesSearched = algorithm.getNodesEvaluated();
		final double secondsTaken = end.minus(start.getMillis()).getMillis() * 0.001;
		System.out.println(nodesSearched);
		
		log.info("{} nodes per second", (int) (nodesSearched / secondsTaken));
	}
}
