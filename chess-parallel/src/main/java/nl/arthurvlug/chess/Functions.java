package nl.arthurvlug.chess;

import java.util.ArrayList;
import java.util.Random;

import org.apache.crunch.CombineFn;
import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.MapFn;
import org.apache.crunch.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Functions {
	private static final int MAX_SCORE = 1000;
	private static final int MOVES_PER_DEPTH = 2;
	
	static final DoFn<TreeNode, TreeNode> ADD_NEW_MOVES = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 1L;
		public void process(final TreeNode node, final Emitter<TreeNode> emitter) {
			for (int i = 0; i < MOVES_PER_DEPTH; i++) {
				final char c = (char) ('A' + i);
				final String move = Character.toString(c);
				emitter.emit(new TreeNode(move, node));
			}
		}
	};

	static final DoFn<String, TreeNode> ROOT_TO_TREENODE = new DoFn<String, TreeNode>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String _, final Emitter<TreeNode> emitter) {
			emitter.emit(TreeNode.ROOT_MIN);
		}
	};

	static final MapFn<TreeNode, String> ADD_PARENT_AS_KEY = new MapFn<TreeNode, String>() {
		private static final long serialVersionUID = 8871734393164750058L;

		@Override
		public String map(final TreeNode input) {
			final TreeNode parent = input.getParent();
			if(parent == null) {
				return "";
			}
			return parent.getCurrentAndAncestors();
		}
	};
	
	@SuppressWarnings("serial")
	static final DoFn<TreeNode, TreeNode> RANDOM_SCORE = new MapFn<TreeNode, TreeNode>() {
		@Override
		public TreeNode map(final TreeNode input) {
			final int hashCode = input.getCurrentAndAncestors().hashCode();
			final int score = new Random(hashCode).nextInt(MAX_SCORE);
			input.setScore(score);
			return input;
		}
	};
	static final CombineFn<String, TreeNode> PARENT_TAKE_BEST_MOVE = new CombineFn<String, TreeNode>() {
		private static final long serialVersionUID = -621144572607844850L;

		@Override
		public void process(final Pair<String, Iterable<TreeNode>> input, final Emitter<Pair<String, TreeNode>> emitter) {
			final ArrayList<TreeNode> children = Lists.newArrayList(input.second());
			
			Function<Pair<TreeNode, TreeNode>, Boolean> better = better(input);
			
			TreeNode maxNode = better == MIN ? TreeNode.ROOT_MAX : TreeNode.ROOT_MIN;
			for(final TreeNode node : children) {
				if(better.apply(new Pair<TreeNode, TreeNode>(node, maxNode))) {
					maxNode = node;
				}
			}
			final Pair<String, TreeNode> pair = new Pair<String, TreeNode>(input.first(), maxNode);
			emitter.emit(pair);
		}

		private Function<Pair<TreeNode, TreeNode>, Boolean> better(Pair<String, Iterable<TreeNode>> input) {
			return input.first().length() % 2 == 0 ? MAX : MIN;
		}

		protected final Function<Pair<TreeNode, TreeNode>, Boolean> MAX = new Function<Pair<TreeNode, TreeNode>, Boolean>() {
			public Boolean apply(Pair<TreeNode, TreeNode> input) {
				return input.first().getScore() > input.second().getScore();
			}
		};
		protected final Function<Pair<TreeNode, TreeNode>, Boolean> MIN = new Function<Pair<TreeNode, TreeNode>, Boolean>() {
			public Boolean apply(Pair<TreeNode, TreeNode> input) {
				return input.first().getScore() < input.second().getScore();
			}
		};
	};
	
	static final DoFn<TreeNode, TreeNode> GET_PARENT = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 5358247606242228527L;

		@Override
		public void process(final TreeNode node, final Emitter<TreeNode> emitter) {
			node.getParent().setScore(node.getScore());
			emitter.emit(node.getParent());
		}
	};
}
