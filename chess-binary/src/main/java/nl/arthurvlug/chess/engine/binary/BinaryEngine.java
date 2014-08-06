package nl.arthurvlug.chess.engine.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.UCIEngine;
import nl.arthurvlug.chess.util.Markers;

import com.google.common.base.Preconditions;

@Slf4j
public abstract class BinaryEngine extends UCIEngine {
	private final String fileName;

	// Process
	private Process p;

	public BinaryEngine(final String fileName) {
		this.fileName = Preconditions.checkNotNull(fileName);
	}

	@Override
	protected void initializeEngine() throws IOException {
		final String resourcePath = getClass().getResource("/engines/" + fileName).getFile().toString();
		final String command = "wine64 " + resourcePath;
		p = Runtime.getRuntime().exec(command);
	}

	@Override
	protected void internalHandleShutdownEvent() {
		p.destroy();
		log.info(Markers.ENGINE, getName() + " -    Engine down");
	}

	@Override
	protected OutputStream getCommandStream() {
		return p.getOutputStream();
	}

	@Override
	protected InputStream getErrorStream() {
		return p.getErrorStream();
	}

	@Override
	protected InputStream getOutputStream() {
		return p.getInputStream();
	}
}
