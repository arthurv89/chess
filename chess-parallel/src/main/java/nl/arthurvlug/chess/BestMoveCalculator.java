package nl.arthurvlug.chess;

import static nl.arthurvlug.chess.Functions.ADD_NEW_POSITIONS;
import static nl.arthurvlug.chess.Functions.ADD_PARENT_AS_KEY;
import static nl.arthurvlug.chess.Functions.GET_PARENT;
import static nl.arthurvlug.chess.Functions.PARENT_TAKE_BEST_MOVE;
import static nl.arthurvlug.chess.Functions.ROOT_TO_POSITION;
import static nl.arthurvlug.chess.Functions.SCORE_POSITIONS;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.PCollection;
import org.apache.crunch.PGroupedTable;
import org.apache.crunch.PTable;
import org.apache.crunch.io.text.TextFileTarget;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;
import org.apache.hadoop.fs.Path;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class BestMoveCalculator implements Serializable {
	private static final long serialVersionUID = -6683089099880990848L;
	
	private final int depth;
	private static final AvroType<String> STRING_TYPE = Avros.records(String.class);
	public static final AvroType<Position> TREE_NODE_TYPE = Avros.records(Position.class);

	
	private final PCollection<String> initialPosition;

	public BestMoveCalculator(final PCollection<String> initialPosition, int depth) {
		this.initialPosition = initialPosition;
		this.depth = depth;
	}


	public PCollection<Position> createSuccessorPositions() {
		PCollection<Position> currentNodeCollection = initialPosition.parallelDo(ROOT_TO_POSITION, TREE_NODE_TYPE);

		materialize(currentNodeCollection);
		for (int i = 0; i < depth; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_POSITIONS, TREE_NODE_TYPE);
		}
		
		return currentNodeCollection;
	}


	public PCollection<Position> moveScores(PCollection<Position> positions) {
		for (int i = 0; i < depth-1; i++) {
			positions = positions
					.by(ADD_PARENT_AS_KEY, STRING_TYPE)
					.groupByKey()
					.combineValues(PARENT_TAKE_BEST_MOVE)
					.values()
					.parallelDo(GET_PARENT, TREE_NODE_TYPE);
			
//			PTable<String, Position> positions2 = positions.by(ADD_PARENT_AS_KEY, STRING_TYPE);
//			System.out.println("2: " + toList(positions2));
//			
//			PGroupedTable<String, Position> positions3 = positions2.groupByKey();
//			toList(positions3);
//			
//			PTable<String, Position> x = positions3.combineValues(Functions.X);
//			System.out.println("X: " + toList(x));
//
//			PTable<String, Position> positions4 = positions3.combineValues(PARENT_TAKE_BEST_MOVE);
//			System.out.println("4: " + toList(positions4));
//			
//			PCollection<Position> positions5 = positions4.values();
//			System.out.println("5: " + toList(positions5));
//			
//			positions = positions5.parallelDo(GET_PARENT, TREE_NODE_TYPE);
//			System.out.println("New: " + toList(positions));
		}
		
		return positions;
	}


	public PCollection<Position> scoredPositions(final PCollection<Position> positions) {
		return positions.parallelDo(SCORE_POSITIONS, TREE_NODE_TYPE);
	}

//	private <T> ArrayList<T> toList(PGroupedTable<String, Position> groupedTable) {
//		Path target = new Path("grouped");
//		
//		try {
//			FileUtils.deleteDirectory(new File("grouped"));
//
//			groupedTable.write(new TextFileTarget(target));
//		} catch(Exception e) {
//			throw Throwables.propagate(e);
//		}
//		return null;
//	}

	public static <T> ArrayList<T> materialize(PCollection<T> collection) {
		return Lists.newArrayList(collection.materialize());
	}
}
