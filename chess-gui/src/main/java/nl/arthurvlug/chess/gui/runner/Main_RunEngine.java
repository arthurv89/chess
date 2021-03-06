package nl.arthurvlug.chess.gui.runner;
import com.google.common.base.Splitter;
import com.google.common.eventbus.EventBus;
import java.util.List;
import nl.arthurvlug.chess.engine.ace.ACE;

public class Main_RunEngine {
	public static void main(String[] args) {
		thinkMoves("e2e4 e7e5 f2f4 f7f5 b1c3 g8f6 d1e2");
	}

	private static void thinkMoves(String sMoves) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		
		ACE ace = new ACE(5, "BLACK", new EventBus());

		ace.startThinking();
	}
}
