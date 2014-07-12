package nl.arthurvlug.chess.gui;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public abstract class Window extends JFrame {
	public void open() {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}
	
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
