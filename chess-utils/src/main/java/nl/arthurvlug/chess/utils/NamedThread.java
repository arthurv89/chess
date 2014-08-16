package nl.arthurvlug.chess.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamedThread extends Thread {
	public NamedThread(Runnable runnable, String name) {
		super(runnable);
		setName(name + " " + this.getId());
	}
	
	@Override
	public void run() {
		try {
			super.run();
		} catch(Exception e) {
			log.error("Unknown exception in Thread", e);
			throw e;
		}
	}
}
