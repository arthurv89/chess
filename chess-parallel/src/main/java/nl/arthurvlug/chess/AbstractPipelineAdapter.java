package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.crunch.Pipeline;

@AllArgsConstructor
public abstract class AbstractPipelineAdapter {
	@Getter
	private Pipeline pipeline;

	public abstract List<String> parseResult(File outputFolder) throws IOException;
}
