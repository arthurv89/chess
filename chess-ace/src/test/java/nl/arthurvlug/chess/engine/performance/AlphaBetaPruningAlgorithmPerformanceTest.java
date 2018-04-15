package nl.arthurvlug.chess.engine.performance;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;

import org.joda.time.DateTime;

@Slf4j
public class AlphaBetaPruningAlgorithmPerformanceTest {
	public static void main(String[] args) {
		for (int i = 1; i < 10; i++) {
			performanceTest(i);
		}
	}

	private static void performanceTest(int depth) {
		log.debug("");
		log.debug("Starting performance test for depth {}", depth);
		final InitialACEBoard board = new InitialACEBoard();
		board.finalizeBitboards();
		final AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new AceConfiguration());
		
		final DateTime start = DateTime.now();
		algorithm.think(board);
		final DateTime end = DateTime.now();
		
		final int nodesSearched = algorithm.getNodesEvaluated();
		final double secondsTaken = end.minus(start.getMillis()).getMillis() * 0.001;
		
		
		log.info("{} nodes searched", nodesSearched);
		log.info("{} cut-offs", algorithm.getCutoffs());
		log.info("{} nodes per second", (int) (nodesSearched / secondsTaken));
	}
}
