import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.arthurvlug.chess.BestMoveCalculator;
import nl.arthurvlug.chess.Position;

import org.apache.crunch.PCollection;
import org.apache.crunch.impl.mr.MRPipeline;
import org.junit.Test;

public class BestMoveCalculatorTest {
	@Test
	public void createSuccessorPositions() {
		MRPipeline pipeline = new MRPipeline(BestMoveCalculator.class);
		final PCollection<String> initialPosition = pipeline.readTextFile("input.txt");
		
		BestMoveCalculator calculator = new BestMoveCalculator(initialPosition, 2);
		final PCollection<Position> successorPositions = calculator.createSuccessorPositions();
		
		List<Position> expectedPositions = Arrays.asList(
				new Position("a7a5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("b7b5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("c7c5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("d7d5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("e7e5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("f7f5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("g7g5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("h7h5", new Position("a2a4", Position.MIN_POSITION)),
				new Position("a7a5", new Position("b2b4", Position.MIN_POSITION))
		);
		ArrayList<Position> positions = BestMoveCalculator.materialize(successorPositions);
		assertEquals(64, positions.size());
		assertEquals(expectedPositions, positions.subList(0, 9));
	}

	@Test
	public void scoredPositionsTest() {
		MRPipeline pipeline = new MRPipeline(BestMoveCalculator.class);
		final PCollection<String> initialPosition = pipeline.readTextFile("input.txt");
		
		BestMoveCalculator calculator = new BestMoveCalculator(initialPosition, 2);
		final PCollection<Position> successorPositions = calculator.createSuccessorPositions();
		final PCollection<Position> scoredPositions = calculator.scoredPositions(successorPositions);

		List<Position> expectedPositions = Arrays.asList(
				new Position("a7a5", new Position("a2a4", Position.MIN_POSITION)).setScore(0),
				new Position("b7b5", new Position("a2a4", Position.MIN_POSITION)).setScore(10),
				new Position("c7c5", new Position("a2a4", Position.MIN_POSITION)).setScore(0),
				new Position("d7d5", new Position("a2a4", Position.MIN_POSITION)).setScore(-55),
				new Position("e7e5", new Position("a2a4", Position.MIN_POSITION)).setScore(-55),
				new Position("f7f5", new Position("a2a4", Position.MIN_POSITION)).setScore(0),
				new Position("g7g5", new Position("a2a4", Position.MIN_POSITION)).setScore(10),
				new Position("h7h5", new Position("a2a4", Position.MIN_POSITION)).setScore(0),
				new Position("a7a5", new Position("b2b4", Position.MIN_POSITION)).setScore(-10)
		);
		ArrayList<Position> positions = BestMoveCalculator.materialize(scoredPositions);
		assertEquals(64, positions.size());
		assertEquals(expectedPositions + "", positions.subList(0, 9) + "");
	}

	@Test
	public void moveScoresTest() {
		MRPipeline pipeline = new MRPipeline(BestMoveCalculator.class);
		final PCollection<String> initialPosition = pipeline.readTextFile("input.txt");
		
		BestMoveCalculator calculator = new BestMoveCalculator(initialPosition, 2);
		final PCollection<Position> successorPositions = calculator.createSuccessorPositions();
		final PCollection<Position> scoredPositions = calculator.scoredPositions(successorPositions);
		final PCollection<Position> bestPositions = calculator.moveScores(scoredPositions);
		
		List<Position> expectedPositions = Arrays.asList(
				new Position("a2a4", Position.MIN_POSITION).setScore(-55),
				new Position("b2b4", Position.MIN_POSITION).setScore(-65),
				new Position("c2c4", Position.MIN_POSITION).setScore(-55),
				new Position("d2d4", Position.MIN_POSITION).setScore(0),
				new Position("e2e4", Position.MIN_POSITION).setScore(0),
				new Position("f2f4", Position.MIN_POSITION).setScore(-55),
				new Position("g2g4", Position.MIN_POSITION).setScore(-65),
				new Position("h2h4", Position.MIN_POSITION).setScore(-55)
		);
		ArrayList<Position> positions = BestMoveCalculator.materialize(bestPositions);
		assertEquals(8, positions.size());
		assertEquals(expectedPositions + "", positions + "");
	}
}
