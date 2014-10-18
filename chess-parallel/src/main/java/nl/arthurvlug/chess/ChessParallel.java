package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.PCollection;
import org.apache.crunch.impl.mem.collect.MemCollection;
import org.apache.crunch.impl.mr.MRPipeline;
import org.apache.crunch.lib.Sort;
import org.apache.crunch.lib.Sort.Order;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class ChessParallel implements Serializable {
	private static final String INPUT_FILE = "input.txt";
	
	private static final String OUTPUT_FOLDER = "output";
	private static final String OUTPUT_FILE = "part-r-00000";

	static {{
		final File file = new File(INPUT_FILE);
		Preconditions.checkArgument(file.exists());
	}}


	public static void main(final String[] args) throws IOException {
//		distributedCalculation();
		memoryCalculation();
	}


	private static void memoryCalculation() {
		MemCollection<String> firstNode = new MemCollection<>(Arrays.asList(""));
		System.out.println(bestMoves(firstNode));
	}


	private static void distributedCalculation() throws IOException {
		File outputFile = new File(OUTPUT_FOLDER);
		FileUtils.deleteDirectory(outputFile);
		
		final MRPipeline pipeline = new MRPipeline(BestMoveCalculator.class);
		final PCollection<String> firstNode = pipeline.readTextFile(INPUT_FILE);
		
		pipeline.writeTextFile(bestMoves(firstNode), OUTPUT_FOLDER);
		pipeline.done();
		
		String result = FileUtils.readFileToString(new File(outputFile, OUTPUT_FILE));
		System.out.println(result);
	}


	private static PCollection<TreeNode> bestMoves(PCollection<String> firstNode) {
		BestMoveCalculator recursive = new BestMoveCalculator(firstNode);
		PCollection<TreeNode> moves = recursive.createMoves();
		PCollection<TreeNode> bestMoves = recursive.bestMove(moves);
		PCollection<TreeNode> bestMovesSorted = Sort.sort(bestMoves, Order.DESCENDING);
		return bestMovesSorted;
	}
}