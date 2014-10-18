package nl.arthurvlug.chess;
import java.util.ArrayList;
import java.util.Random;

import org.apache.crunch.CombineFn;
import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.MapFn;
import org.apache.crunch.PCollection;
import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;

import com.google.common.collect.Lists;

public class BestMoveCalculator {
	private static final int MAX_SCORE = 1000;
	private static final int MOVES_PER_DEPTH = 10;
	private static final int depth = 4;
	private static final AvroType<String> STRING_TYPE = Avros.records(String.class);
	private static final AvroType<TreeNode> TREE_NODE_TYPE = Avros.records(TreeNode.class);

	private static final DoFn<TreeNode, TreeNode> ADD_NEW_MOVES = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 1L;
		public void process(final TreeNode node, final Emitter<TreeNode> emitter) {
			for (int i = 0; i < MOVES_PER_DEPTH; i++) {
				char c = (char) ('A' + i);
				String move = Character.toString(c);
				emitter.emit(new TreeNode(move, node));
			}
		}
	};

	private static final DoFn<String, TreeNode> CONVERT_TO_TREE_NODE = new DoFn<String, TreeNode>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String _, final Emitter<TreeNode> emitter) {
			emitter.emit(TreeNode.root());
		}
	};

	private static final MapFn<TreeNode, String> ADD_PARENT_AS_KEY = new MapFn<TreeNode, String>() {
		private static final long serialVersionUID = 8871734393164750058L;

		@Override
		public String map(TreeNode input) {
			TreeNode parent = input.getParent();
			if(parent == null) {
				return "";
			}
			return parent.getCurrentAndAncestors();
		}
	};
	
//	@SuppressWarnings("serial")
//	private static final MapFn<TreeNode, TreeNode> TO_PTABLE = new MapFn<TreeNode, TreeNode>() {
//		@Override
//		public TreeNode map(TreeNode input) {
//			return null;
//		}
//	};

	@SuppressWarnings("serial")
	private static final DoFn<TreeNode, TreeNode> RANDOM_SCORE = new MapFn<TreeNode, TreeNode>() {
		@Override
		public TreeNode map(TreeNode input) {
			int hashCode = input.getCurrentAndAncestors().hashCode();
			int score = new Random(hashCode).nextInt(MAX_SCORE);
			input.setScore(score);
			return input;
		}
	};
	private static final CombineFn<String, TreeNode> AGGREGATE_BEST_MOVE = new CombineFn<String, TreeNode>() {
		private static final long serialVersionUID = -621144572607844850L;

		@Override
		public void process(Pair<String, Iterable<TreeNode>> input, Emitter<Pair<String, TreeNode>> emitter) {
//			System.out.println("Parent: [" + input.first() + "]");
			ArrayList<TreeNode> children = Lists.newArrayList(input.second());
//			System.out.println("Children: [" + children + "]");
			TreeNode maxNode = TreeNode.root();
			for(TreeNode node : children) {
				if(node.getScore() > maxNode.getScore()) {
					maxNode = node;
				}
//				System.out.println(Lists.newArrayList(input.second()));
			}
			Pair<String, TreeNode> pair = new Pair<String, TreeNode>(input.first(), maxNode);
//			System.out.println("Emit group: [" + pair + "]");
			emitter.emit(pair);
		}
	};
	
	private static final DoFn<TreeNode, TreeNode> GET_PARENT = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 5358247606242228527L;

		@Override
		public void process(TreeNode node, Emitter<TreeNode> emitter) {
			node.getParent().setScore(node.getScore());
//			System.out.println("Get parent of " + node.getCurrentAndAncestors() + " (v=" + node.getScore() + ") = " + node.getParent());
			emitter.emit(node.getParent());
		}
	};
//	private static final DoFn<Pair, Pair> PRINT_GROUP = new DoFn<Pair, Pair>() {
//		private static final long serialVersionUID = 40879573779998720L;
//
//		public void process(final Pair input, final Emitter<Pair> emitter) {
//			ArrayList<TreeNode> list = Lists.newArrayList((TreeNode) input.second());
//			System.out.println("Print group: {" + input.first() + " - " + list);
//			emitter.emit(input);
//		}
//	};
	private final PCollection<String> firstNode;
	
	public BestMoveCalculator(PCollection<String> firstNode) {
		this.firstNode = firstNode;
	}


	PCollection<TreeNode> createMoves() {
		PCollection<TreeNode> currentNodeCollection = firstNode.parallelDo(CONVERT_TO_TREE_NODE, TREE_NODE_TYPE);

//		System.out.println("Root: " + list(currentNodeCollection));
		
		for (int i = 0; i < depth; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_MOVES, TREE_NODE_TYPE);
//			System.out.println("Current collection " + list(currentNodeCollection));
//			System.out.println("Added moves " + list(currentNodeCollection));
		}
		
		return currentNodeCollection;
	}


	PCollection<TreeNode> bestMove(PCollection<TreeNode> moves) {
		moves = moves.parallelDo(RANDOM_SCORE, TREE_NODE_TYPE);
//		System.out.println("Best moves for " + list(moves));
	
		for (int i = 0; i < depth-1; i++) {
//			System.out.println("");
//			System.out.println("");
			PTable<String, TreeNode> grouped = moves
					.by(ADD_PARENT_AS_KEY, STRING_TYPE);
//			System.out.println("By " + list(grouped));
			
//			grouped.groupByKey().parallelDo(PRINT_GROUP, GROUP_TYPE);
			
			moves = grouped
					.groupByKey()
					.combineValues(AGGREGATE_BEST_MOVE)
					.values();
//			System.out.println("Max parent nodes: " + list(moves));
//			
			moves = moves.parallelDo(GET_PARENT, TREE_NODE_TYPE);
//			System.out.println("Best parent: " + list(moves));
		}
		
		return moves;
	}


//	private <T> ArrayList<T> list(PCollection<T> moves) {
//		return Lists.newArrayList(moves.materialize());
//	}
}
