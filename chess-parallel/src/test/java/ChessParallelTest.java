import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import nl.arthurvlug.chess.ChessParallel;

import org.junit.jupiter.api.Test;

public class ChessParallelTest {
	private static final double DELTA = 0.001;

	@Test
	public void testMapReduce1() throws Exception {
		ChessParallel parallel = new ChessParallel(1);
		List<String> result = parallel.mapReduce();

		assertEquals(-0.05, score("a2a4", result), DELTA);
		assertEquals(-0.05, score("h2h4", result), DELTA);
		assertEquals(0.50, score("e2e4", result), DELTA);
		assertEquals(0.50, score("d2d4", result), DELTA);
	}

	@Test
	public void testMapReduce2() throws Exception {
		ChessParallel parallel = new ChessParallel(2);
		List<String> result = parallel.mapReduce();
		
		assertEquals(-0.55, score("a2a4", result), DELTA);
		assertEquals(-0.55, score("h2h4", result), DELTA);
		assertEquals(0.00, score("d2d4", result), DELTA);
		assertEquals(0.00, score("e2e4", result), DELTA);
	}

	
	
	
	
	
	@Test
	public void testMem1() throws Exception {
		ChessParallel parallel = new ChessParallel(1);
		List<String> result = parallel.mem();

		assertEquals(-0.05, score("a2a4", result), DELTA);
		assertEquals(-0.05, score("h2h4", result), DELTA);
		assertEquals(0.50, score("e2e4", result), DELTA);
		assertEquals(0.50, score("d2d4", result), DELTA);
	}

	@Test
	public void testMem2() throws Exception {
		ChessParallel parallel = new ChessParallel(2);
		List<String> result = parallel.mem();

		assertEquals(-0.55, score("a2a4", result), DELTA);
		assertEquals(-0.55, score("h2h4", result), DELTA);
		assertEquals(0.00, score("e2e4", result), DELTA);
		assertEquals(0.00, score("d2d4", result), DELTA);
	}

	private double score(String move, List<String> result) {
		String s = result.stream().filter(x -> x.startsWith(move)).findFirst().get();
		String sValue = s.substring(s.indexOf('=')+1, s.length()-1);
		return Double.valueOf(sValue);
	}
}
