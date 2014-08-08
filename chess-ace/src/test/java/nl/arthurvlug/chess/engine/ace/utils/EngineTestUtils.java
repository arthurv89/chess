package nl.arthurvlug.chess.engine.ace.utils;
import java.util.List;

import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.game.Move;

import com.google.common.base.Function;
import com.google.common.collect.Lists;


public class EngineTestUtils {
	private static final Function<AceMove, Move> ENGINE_MOVE_TO_MOVE = new Function<AceMove, Move>() {
		@Override
		public Move apply(AceMove move) {
			return move.toMove();
		}
	};
	
	public static List<Move> engineMovesToMoves(List<AceMove> aceMoves) {
		return Lists.transform(aceMoves, ENGINE_MOVE_TO_MOVE);
	}

	public static Move engineMoveToMove(AceMove move) {
		return ENGINE_MOVE_TO_MOVE.apply(move);
	}
	
	public static void compare(long board1, long board2) {
		System.out.println(BitboardUtils.toBitboardString(board1));
		System.out.println(BitboardUtils.toBitboardString(board2));
	}
}
