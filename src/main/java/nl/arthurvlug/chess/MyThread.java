package nl.arthurvlug.chess;

public class MyThread extends Thread {
	public MyThread(Runnable runnable, String name) {
		super(runnable);
		setName(name + " " + this.getId());
	}
}
