package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.Pipeline;

public class MemPipelineAdapter extends AbstractPipelineAdapter {
	public MemPipelineAdapter(Pipeline pipeline) {
		super(pipeline);
	}

	@Override
	public List<String> parseResult(File outputFolder) throws IOException {
		Path path = FileSystems.getDefault().getPath(outputFolder.getAbsolutePath());
		Path file = Files.list(path).filter(p -> {
				String filename = p.getFileName().getFileName().toString();
				return filename.startsWith("out") && filename.endsWith(".txt");
			}
		).findFirst().get();
		return FileUtils.readLines(file.toFile());
	}
}
