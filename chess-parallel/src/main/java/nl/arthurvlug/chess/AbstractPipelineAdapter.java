package nl.arthurvlug.chess;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.commons.io.FileUtils;
import org.apache.crunch.Pipeline;

import com.google.common.base.Throwables;

@AllArgsConstructor
public abstract class AbstractPipelineAdapter {
	@Getter
	private Pipeline pipeline;

	public List<String> parseResult(File outputFolder) throws IOException {
		Path path = FileSystems.getDefault().getPath(outputFolder.getAbsolutePath());
		return Files.list(path)
			.filter(p -> acceptFile(p.getFileName().getFileName().toString()))
			.map(p -> {
				try {
					return FileUtils.readLines(p.toFile());
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			})
			.reduce(new ArrayList<String>(), (t, u) -> {
				List<String> list = new ArrayList<String>();
				list.addAll(t);
				list.addAll(u);
				return list;
			});
	}


	protected abstract boolean acceptFile(String filename);
}
