package nl.arthurvlug.chess.gui;

import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public abstract class Window extends JFrame {
	public void open() throws FontFormatException, IOException {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}
	
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
