package nl.arthurvlug.chess;
import static nl.arthurvlug.chess.Functions.ADD_NEW_POSITIONS;
import static nl.arthurvlug.chess.Functions.ADD_PARENT_AS_KEY;
import static nl.arthurvlug.chess.Functions.PARENT_TAKE_BEST_MOVE;
import static nl.arthurvlug.chess.Functions.ROOT_TO_POSITION;
import static nl.arthurvlug.chess.Functions.GET_PARENT;
import static nl.arthurvlug.chess.Functions.SCORE_POSITIONS;

import java.util.ArrayList;

import org.apache.crunch.PCollection;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;

import com.google.common.collect.Lists;

public class BestMoveCalculator {
	private static final int depth = 3;
	private static final AvroType<String> STRING_TYPE = Avros.records(String.class);
	private static final AvroType<Position> TREE_NODE_TYPE = Avros.records(Position.class);

	
	private final PCollection<String> initialPosition;
	
	public BestMoveCalculator(final PCollection<String> initialPosition) {
		this.initialPosition = initialPosition;
	}


	PCollection<Position> createSuccessorPositions() {
		PCollection<Position> currentNodeCollection = initialPosition.parallelDo(ROOT_TO_POSITION, TREE_NODE_TYPE);
		
		for (int i = 0; i < depth; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_POSITIONS, TREE_NODE_TYPE);
		}
		
		return currentNodeCollection;
	}


	PCollection<Position> moveScores(PCollection<Position> positions) {
		for (int i = 0; i < depth-1; i++) {
			System.out.println(materializeList(positions));
			positions = positions
					.by(ADD_PARENT_AS_KEY, STRING_TYPE)
					.groupByKey()
					.combineValues(PARENT_TAKE_BEST_MOVE)
					.values()
					.parallelDo(GET_PARENT, TREE_NODE_TYPE);
		}
		System.out.println(materializeList(positions));
		
		return positions;
	}


	public PCollection<Position> scoredPositions(final PCollection<Position> positions) {
		return positions.parallelDo(SCORE_POSITIONS, TREE_NODE_TYPE);
	}


	private <T> ArrayList<T> materializeList(PCollection<T> list) {
		return Lists.newArrayList(list.materialize());
	}
}
