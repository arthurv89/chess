package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.Pipeline;

public class MRPipelineAdapter extends AbstractPipelineAdapter {
	public MRPipelineAdapter(Pipeline pipeline) {
		super(pipeline);
	}

	public List<String> parseResult(File outputFolder) throws IOException {
		return FileUtils.readLines(new File(outputFolder, "part-m-00000"));
	}
}
