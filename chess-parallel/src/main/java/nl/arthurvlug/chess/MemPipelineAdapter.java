package nl.arthurvlug.chess;

import org.apache.crunch.Pipeline;


public class MemPipelineAdapter extends AbstractPipelineAdapter {
	public MemPipelineAdapter(Pipeline pipeline) {
		super(pipeline);
	}

	protected boolean acceptFile(String filename) {
		return filename.startsWith("out") && filename.endsWith(".txt");
	}
}
