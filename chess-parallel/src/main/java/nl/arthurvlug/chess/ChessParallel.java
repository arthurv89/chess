package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.PCollection;
import org.apache.crunch.Pipeline;
import org.apache.crunch.impl.mr.MRPipeline;

public abstract class ChessParallel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final String OUTPUT_FOLDER = "output";
	private static final String OUTPUT_FILE = "part-m-00000";


	public static void main(final String[] args) throws IOException {
		File outputFile = new File(OUTPUT_FOLDER);
		FileUtils.deleteDirectory(outputFile);
		
		final Pipeline pipeline = new MRPipeline(Recursive.class);

		PCollection<TreeNode> moves = Recursive.createMoves(pipeline);
		PCollection<TreeNode> bestMoves = Recursive.bestMove(moves);
		System.out.println("Max nodes: " + bestMoves.materialize());
		
		pipeline.writeTextFile(moves, OUTPUT_FOLDER);
		pipeline.done();
		
		String result = FileUtils.readFileToString(new File(outputFile, OUTPUT_FILE));
//		System.err.println(result);
	}
}