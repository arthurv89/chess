package nl.arthurvlug.chess.engine.performance;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;

import org.joda.time.DateTime;

@Slf4j
public class PerformanceRunner {
	private static final int BOARD_EVALUATION_NUMBER = 1000000;

	public static void run(AbstractEngineBoard board, BoardEvaluator aceEvaluator) {
		DateTime start = DateTime.now();
		for (int i = 0; i < BOARD_EVALUATION_NUMBER; i++) {
			aceEvaluator.evaluate(board);
		}
		DateTime end = DateTime.now();
		double secondsTaken = end.minus(start.getMillis()).getMillis() * 0.001;
		
		log.info("{} nodes per second", (int) (BOARD_EVALUATION_NUMBER / secondsTaken));
	}
}
