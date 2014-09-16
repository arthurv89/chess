package nl.arthurvlug.chess.engine.ace;

import java.util.List;
import java.util.function.Function;

import org.junit.Test;

public class ACETest {
	@Test
	public void testTrap_Depth2() {
		checkAceMove("b1c3 b8c6 g1f3 e7e5", not("f3e5"), 1);
	}
	
	@Test
	public void testTrap_Depth1() {
		checkAceMove("b1c3 b8c6 g1f3 e7e5", not("f3e5"), 1);
	}
	
	@Test
	public void testTrap_Depth1_black() {
		checkAceMove("e2e4 g8f6 b1c3", not("f6e4"), 1);
	}

	@Test
	public void testTrap_Depth2_black() {
		checkAceMove("e2e4 g8f6 b1c3", not("f6e4"), 2);
	}

	@Test
	public void testDontLetOpponentTakeKnight() {
		checkAceMove("b1a3 b8c6 g1f3 g8f6 a3b1 e7e5 b1a3 e5e4", is("f3g1"), 2);
	}

	
	@Test
	public void testTrap_Depth1_black() {
		List<String> moves = Splitter.on(' ').splitToList("e2e4 g8f6 b1c3");
		
		ACE ace = new ACE();
		ace.depth = 1;
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertFalse(move.toString().equals("f6e4"));
	
	
	private Function<Move, Boolean> is(String string) {
		return move -> move.equals(string);
	}

	private Function<Move, Boolean> not(final String string) {
		return move -> !move.equals(string);
	}

	private void checkAceMove(String sMoves, Function<Move, Boolean> expect, int depth) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		
		ACE ace = new ACE();
		ace.DEPTH = depth;
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
		assertTrue(expect.apply(move));
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
