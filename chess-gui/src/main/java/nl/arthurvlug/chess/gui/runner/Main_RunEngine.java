package nl.arthurvlug.chess.gui.runner;
import nl.arthurvlug.chess.engine.ace.ACE;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;

import com.google.common.collect.ImmutableList;

public class Main_RunEngine {
	public static void main(String[] args) {
		ACE ace = new ACE();
		ace.setDepth(5);
		ace.think(ImmutableList.<String>of(), new ThinkingParams());
	}
}
