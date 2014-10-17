package nl.arthurvlug.chess;
import java.io.File;

import org.apache.crunch.CombineFn;
import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.MapFn;
import org.apache.crunch.PCollection;
import org.apache.crunch.PGroupedTable;
import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.Pipeline;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Recursive {
	private static final AvroType<String> GROUP_BY_KEY = Avros.records(String.class);
	private static final AvroType<TreeNode> TREE_NODE_TYPE = Avros.records(TreeNode.class);

	//	@SuppressWarnings("unchecked")
//	private static final Class<Pair<TreeNode, TreeNode>> PAIR_TYPE = (Class<Pair<TreeNode, TreeNode>>) new Pair<TreeNode, TreeNode>(null, null).getClass();

	private static final String INPUT_FILE = "input.txt";
	static {{
		final File file = new File(INPUT_FILE);
		Preconditions.checkArgument(file.exists());
	}}

	private static final int iterations = 2;

	private static final DoFn<TreeNode, TreeNode> ADD_NEW_MOVES = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 1L;
		public void process(final TreeNode line, final Emitter<TreeNode> emitter) {
			emitter.emit(new TreeNode("A", line.getParent()));
			emitter.emit(new TreeNode("B", line.getParent()));
			emitter.emit(new TreeNode("C", line.getParent()));
		}
	};

	private static final DoFn<String, TreeNode> CONVERT_TO_TREE_NODE = new DoFn<String, TreeNode>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String _, final Emitter<TreeNode> emitter) {
			emitter.emit(TreeNode.root());
		}
	};

	private static final MapFn<TreeNode, String> GROUP_BY_PARENT = new MapFn<TreeNode, String>() {
		private static final long serialVersionUID = 8871734393164750058L;

		@Override
		public String map(TreeNode input) {
			String ancestors = input.getParent().toString();
			return ancestors;
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
	private static final DoFn<TreeNode, TreeNode> SCORE = new MapFn<TreeNode, TreeNode>() {
		@Override
		public TreeNode map(TreeNode input) {
			input.setScore();
			return input;
		}
	};
	private static final CombineFn<String, TreeNode> AGGREGATE_BEST_MOVE = new CombineFn<String, TreeNode>() {
		private static final long serialVersionUID = -621144572607844850L;

		@Override
		public void process(Pair<String, Iterable<TreeNode>> input, Emitter<Pair<String, TreeNode>> emitter) {
			int maxValue = Integer.MIN_VALUE;
			TreeNode maxNode = null;
			for(TreeNode node : input.second()) {
				if(node.getValue() > maxValue) {
					maxValue = node.getValue();
					maxNode = node;
				}
				System.out.println(input.second());
			}
			Pair<String, TreeNode> pair = new Pair<String, TreeNode>(input.first(), maxNode);
			System.out.println("Emit group: " + pair);
			emitter.emit(pair);
		}
	};
	
	private static final DoFn<TreeNode, TreeNode> GET_PARENT = new DoFn<TreeNode, TreeNode>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5358247606242228527L;

		@Override
		public void process(TreeNode input, Emitter<TreeNode> emitter) {
			emitter.emit(input.getParent());
		}
	};


	 
	
	static PCollection<TreeNode> createMoves(Pipeline pipeline) {
		PCollection<TreeNode> currentNodeCollection = pipeline.readTextFile(INPUT_FILE).parallelDo(CONVERT_TO_TREE_NODE, TREE_NODE_TYPE);
		System.out.println("Root: " + Lists.newArrayList(currentNodeCollection.materialize()));

		for (int i = 0; i < iterations; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_MOVES, TREE_NODE_TYPE);
			System.out.println("Added moves " + Lists.newArrayList(currentNodeCollection.materialize()));
		}
		
		return currentNodeCollection;
	}


	static PCollection<TreeNode> bestMove(PCollection<TreeNode> moves) {
		for (int i = 0; i < 10; i++) {
			System.out.println("Max nodes: " + moves.materialize());
			final PCollection<TreeNode> scoredMoves = moves.parallelDo(SCORE, TREE_NODE_TYPE);
			final PGroupedTable<String, TreeNode> movesGroupedByParent = scoredMoves
					.by(GROUP_BY_PARENT, GROUP_BY_KEY).groupByKey();
			
			final PTable<String, TreeNode> maxNodes = movesGroupedByParent.combineValues(AGGREGATE_BEST_MOVE);
			PCollection<TreeNode> parents = maxNodes.values().parallelDo(GET_PARENT, TREE_NODE_TYPE);
			moves = parents.parallelDo(SCORE, TREE_NODE_TYPE);

		}
		return moves;
//		System.out.println("Max nodes: " + maxNodes.asMap().getValue());
//		System.out.println(Lists.newArrayList(movesGroupedByParent.materialize()));
		
//		final Pair<TreeNode, TreeNode> max = movesGroupedByParent.max().getValue();
//		System.out.println(max);
	}
}
