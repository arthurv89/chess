package nl.arthurvlug.chess.engine.ace;

import java.util.List;
import java.util.function.Function;

import org.junit.Test;

public class ACETest {
	
	@Test
	public void testTrap_Depth1() {
		checkAceMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 1); // After taking a piece, we should do another move
	}
	
	@Test
	public void testTrap_Depth2() {
		checkAceMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 2);
	}
	
	@Test
	public void testTrap_Depth1_black() {
		checkAceMove("e2e4 g8f6 b1c3", not(is("f6e4")), 1); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2_black() {
		checkAceMove("e2e4 g8f6 b1c3", not(is("f6e4")), 2);
	}

	@Test
	public void testDontLetOpponentTakeKnight() {
		checkAceMove("e2e4 g8f6 e4e5", string -> string.toString().startsWith("f6"), 2);
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
		return move -> move.toString().equals(string);
	}

	private Function<Move, Boolean> not(Function<Move, Boolean> function) {
		return move -> !function.apply(move);
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
