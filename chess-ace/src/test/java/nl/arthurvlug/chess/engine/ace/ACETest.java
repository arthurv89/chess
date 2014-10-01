package nl.arthurvlug.chess.engine.ace;

import static org.junit.Assert.*;

import java.util.List;

import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Test;

import com.google.common.base.Splitter;

public class ACETest {
	@Test
	public void testTrap_Depth1() {
		List<String> moves = Splitter.on(' ').splitToList("b1c3 b8c6 g1f3 e7e5");
		
		ACE ace = new ACE();
		ace.depth = 1;
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertFalse(move.toString().equals("f3e5"));
	}

	@Test
	public void testTrap_Depth2() {
		List<String> moves = Splitter.on(' ').splitToList("b1c3 b8c6 g1f3 e7e5");
		
		ACE ace = new ACE();
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertFalse(move.toString().equals("f3e5"));
	}
	
	@Test
	public void testTrap_Depth1_black() {
		List<String> moves = Splitter.on(' ').splitToList("e2e4 g8f6 b1c3");
		
		ACE ace = new ACE();
		ace.depth = 1;
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertFalse(move.toString().equals("f6e4"));
	}

	@Test
	public void testTrap_Depth2_black() {
		List<String> moves = Splitter.on(' ').splitToList("e2e4 g8f6 b1c3");
		
		ACE ace = new ACE();
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertFalse(move.toString().equals("f6e4"));
	}

	@Test
	public void testShouldMoveKnight_white() {
		List<String> moves = Splitter.on(' ').splitToList("g1f3 e7e5 b2b3 e5e4");
		
		ACE ace = new ACE();
		ace.depth = 2;
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertEquals("f3g1", move.toString());
	}
}
