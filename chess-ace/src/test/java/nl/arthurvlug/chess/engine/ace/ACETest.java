package nl.arthurvlug.chess.engine.ace;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Function;

import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

import org.junit.Ignore;
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

	@Ignore
	@Test
	public void testShouldPlayWinningMove() { // e6 fxe6 Ne5 Bxc3 bxc3 [...] Nxd7
//		.♜.♛♚..♜
//		..♟♞♞♟♟♟
//		........
//		♟...♙...
//		♕♝......
//		..♘.♗♘..
//		♙♙...♙♙♙
//		..♔♖...♖

		// Expect to play e5e6
		checkMove("d2d4 b8c6 b1c3 g8f6 g1f3 d7d5 e2e3 c8f5 c1d2 e7e6 f1d3 f5d3 c2d3 f8d6 d1b3 a8b8 e3e4 d5e4 d3e4 e6e5 d4e5 c6e5 b3a4 e5c6 d2e3 a7a5 e4e5 b7b5 c3b5 d6b4 e1c1 f6d7 b5c3 c6e7",
				movesPiece("e5"), 6);
	}

	private void checkMove(String sMoves, Function<Move, Boolean> expect, int depth) {
		List<String> moves = Splitter.on(' ').splitToList(sMoves);
		checkMove(moves, expect, depth);
	}

	private void checkMove(final List<String> moves, final Function<Move, Boolean> expect, final int depth) {
		ACE ace = new ACE(depth, new SimplePieceEvaluator(), 1);
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
