package nl.arthurvlug.chess.engine.performance;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;

import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;

@Slf4j
public class AlphaBetaPruningAlgorithmPerformanceTest {
	public static void main(String[] args) {
		InitialEngineBoard board = new InitialEngineBoard();
		board.apply(Arrays.asList("e2e4"));
		AlphaBetaPruningAlgorithm algorithm = new AlphaBetaPruningAlgorithm(new AceEvaluator());
		
		DateTime start = DateTime.now();
		algorithm.think(board, 1);
		DateTime end = DateTime.now();
		
		int nodesSearched = algorithm.getNodesSearched();
		double secondsTaken = end.minus(start.getMillis()).getMillis() * 0.001;
		
		log.info("{} nodes per second", (int) (nodesSearched / secondsTaken));
	}
}
