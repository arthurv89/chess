package nl.arthurvlug.chess.util;

public class NamedThread extends Thread {
	public NamedThread(Runnable runnable, String name) {
		super(runnable);
		setName(name + " " + this.getId());
	}
}
