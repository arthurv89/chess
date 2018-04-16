package nl.arthurvlug.chess.engine.performance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceRunner {
	private static final int BOARDS = 10000000;

//	public static void run(AbstractEngineBoard<?> board, BoardEvaluator<? extends AbstractEngineBoard, Integer> aceEvaluator) {
//		DateTime start = DateTime.now();
//		for (int i = 0; i < BOARDS; i++) {
//			aceEvaluator.evaluate(board);
//		}
//		DateTime end = DateTime.now();
//		double secondsTaken = end.minus(start.getMillis()).getMillis() * 0.001;
//
//		log.info("{} nodes per second", (int) (BOARDS / secondsTaken));
//	}
}
