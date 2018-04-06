package nl.arthurvlug.chess.engine.ace;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Function;

import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Test;

import com.google.common.base.Splitter;

public class ACETest {
	
	@Test
	public void testTrap_Depth1() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 1); // After taking a piece, we should do another move
	}
	
	@Test
	public void testTrap_Depth2() {
		checkMove("b1c3 b8c6 g1f3 e7e5", not(is("f3e5")), 2);
	}
	
	@Test
	public void testTrap_Depth1_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 1); // After taking a piece, we should do another move
	}

	@Test
	public void testTrap_Depth2_black() {
		checkMove("e2e4 g8f6 b1c3", not(is("f6e4")), 2);
	}

	@Test
	public void testDontLetOpponentTakeKnight() {
		checkMove("e2e4 g8f6 e4e5", movesPiece("f6"), 2);
	}

	@Test
	public void testShouldMoveKnight_white() {
		checkMove("g1f3 e7e5 b2b3 e5e4", movesPiece("f3"), 2);
	}
	
	
	
	


	private void checkMove(String sMoves, Function<Move, Boolean> expect, int depth) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		
		ACE ace = new ACE();
		ace.depth = depth;
		Move move = ace.think(moves, new ThinkingParams());
		assertTrue(expect.apply(move));
	}

	private Function<Move, Boolean> is(String string) {
		return move -> move.toString().equals(string);
	}

	private Function<Move, Boolean> movesPiece(String fromField) {
		return move -> move.toString().startsWith(fromField);
	}

	private Function<Move, Boolean> not(Function<Move, Boolean> function) {
		return move -> !function.apply(move);
	}
}
