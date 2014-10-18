package nl.arthurvlug.chess;
import static nl.arthurvlug.chess.Functions.ADD_NEW_MOVES;
import static nl.arthurvlug.chess.Functions.ADD_PARENT_AS_KEY;
import static nl.arthurvlug.chess.Functions.PARENT_TAKE_BEST_MOVE;
import static nl.arthurvlug.chess.Functions.ROOT_TO_TREENODE;
import static nl.arthurvlug.chess.Functions.GET_PARENT;
import static nl.arthurvlug.chess.Functions.RANDOM_SCORE;

import java.util.ArrayList;

import org.apache.crunch.PCollection;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;

import com.google.common.collect.Lists;

public class BestMoveCalculator {
	private static final int depth = 3;
	private static final AvroType<String> STRING_TYPE = Avros.records(String.class);
	private static final AvroType<TreeNode> TREE_NODE_TYPE = Avros.records(TreeNode.class);

	
	private final PCollection<String> singletonEmptyNode;
	
	public BestMoveCalculator(final PCollection<String> singletonEmptyNode) {
		this.singletonEmptyNode = singletonEmptyNode;
	}


	PCollection<TreeNode> createMoves() {
		PCollection<TreeNode> currentNodeCollection = singletonEmptyNode.parallelDo(ROOT_TO_TREENODE, TREE_NODE_TYPE);
		
		for (int i = 0; i < depth; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_MOVES, TREE_NODE_TYPE);
		}
		
		return currentNodeCollection;
	}


	PCollection<TreeNode> bestMove(PCollection<TreeNode> moves) {
		for (int i = 0; i < depth-1; i++) {
			System.out.println(list(moves));
			moves = moves
					.by(ADD_PARENT_AS_KEY, STRING_TYPE)
					.groupByKey()
					.combineValues(PARENT_TAKE_BEST_MOVE)
					.values()
					.parallelDo(GET_PARENT, TREE_NODE_TYPE);
		}
		System.out.println(list(moves));
		
		return moves;
	}


	public PCollection<TreeNode> scoredMoves(final PCollection<TreeNode> moves) {
		return moves.parallelDo(RANDOM_SCORE, TREE_NODE_TYPE);
	}


	private <T> ArrayList<T> list(PCollection<T> moves) {
		return Lists.newArrayList(moves.materialize());
	}
}
