package nl.arthurvlug.chess;
import java.io.File;

import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.PCollection;
import org.apache.crunch.Pipeline;
import org.apache.crunch.types.avro.Avros;

import com.google.common.base.Preconditions;

public class Recursive {
	private static final String INPUT_FILE = "input.txt";
	static {{
		File file = new File(INPUT_FILE);
		Preconditions.checkArgument(file.exists());
	}}

	private static final int iterations = 10;

	private static final DoFn<TreeNode, TreeNode> ADD_NEW_MOVES = new DoFn<TreeNode, TreeNode>() {
		private static final long serialVersionUID = 1L;
		public void process(final TreeNode line, final Emitter<TreeNode> emitter) {
			emitter.emit(new TreeNode("0", line));
			emitter.emit(new TreeNode("1", line));
			emitter.emit(new TreeNode("2", line));
		}
	};

	private static final DoFn<String, TreeNode> CONVERT_TO_TREE_NODE = new DoFn<String, TreeNode>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String input, final Emitter<TreeNode> emitter) {
			emitter.emit(TreeNode.root());
		}
	};


	 
	
	static PCollection<TreeNode> createMoves(Pipeline pipeline) {
		PCollection<TreeNode> currentNodeCollection = pipeline.readTextFile(INPUT_FILE).parallelDo(CONVERT_TO_TREE_NODE, Avros.records(TreeNode.class));
		for (int i = 0; i < iterations; i++) {
			currentNodeCollection = currentNodeCollection.parallelDo(ADD_NEW_MOVES, Avros.records(TreeNode.class));
		}
		
		return currentNodeCollection;
	}
}
