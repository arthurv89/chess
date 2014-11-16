package nl.arthurvlug.chess;

import org.apache.crunch.Pipeline;

public class MRPipelineAdapter extends AbstractPipelineAdapter {
	public MRPipelineAdapter(Pipeline pipeline) {
		super(pipeline);
	}

	protected boolean acceptFile(String filename) {
		return filename.startsWith("part-");
	}
}
