package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.PCollection;
import org.apache.crunch.Pipeline;
import org.apache.crunch.impl.mem.MemPipeline;
import org.apache.crunch.impl.mr.MRPipeline;

import com.google.common.base.Preconditions;

public class ChessParallel implements Serializable {
	private static final long serialVersionUID = 540472578017394764L;

	private static final String INPUT_FILE = "input.txt";
	private static final String OUTPUT_FOLDER = "output";

	private final int depth;

	static {{
		final File file = new File(INPUT_FILE);
		Preconditions.checkArgument(file.exists());
	}}
	
	public ChessParallel(int depth) {
		this.depth = depth;
	}

	public List<String> mapReduce() throws IOException {
		MRPipelineAdapter pipelineAdapter = new MRPipelineAdapter(new MRPipeline(BestMoveCalculator.class));
		return performPipeline(pipelineAdapter);
	}
	
	public List<String> mem() throws IOException {
		MemPipelineAdapter pipelineAdapter = new MemPipelineAdapter(MemPipeline.getInstance());
		return performPipeline(pipelineAdapter);
	}

	private List<String> performPipeline(AbstractPipelineAdapter pipelineAdapter) throws IOException {
		final File outputFolder = new File(OUTPUT_FOLDER, UUID.randomUUID().toString());
		
		Pipeline pipeline = pipelineAdapter.getPipeline();
		pipeline.enableDebug();
		
		final PCollection<String> initialPosition = pipeline.readTextFile(INPUT_FILE);
		
		PCollection<Position> bestMoveCollection = bestPositions(initialPosition);
		pipeline.writeTextFile(bestMoveCollection, outputFolder.getAbsolutePath());
		pipeline.done();

		List<String> result = pipelineAdapter.parseResult(outputFolder);

		FileUtils.deleteDirectory(outputFolder);
		
		return result;
	}


	private PCollection<Position> bestPositions(final PCollection<String> firstNode) {
		final BestMoveCalculator calculator = new BestMoveCalculator(firstNode, depth);
		final PCollection<Position> positions = calculator.createSuccessorPositions();
		final PCollection<Position> scoredPositions = calculator.scoredPositions(positions);
		final PCollection<Position> bestPositions = calculator.moveScores(scoredPositions);
		return bestPositions;
	}
	
	public static void main(String[] args) throws IOException {
		logDebug(new ChessParallel(2).mem());
	}
}