package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.utils.MoveUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils.createMove;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.initializedBoard;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.piecesToString;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.stringDump;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.defaultUnapplyFlags;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.unapply;
import static nl.arthurvlug.chess.utils.board.pieces.Color.BLACK;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

public class AceBoardTakeTest {
	private ACEBoard engineBoard;
	private final UnapplyFlags unapplyFlags = defaultUnapplyFlags;

	private final String board = """
				....♚...
				........
				........
				........
				........
				......♟.
				.......♙
				....♔...""";

	@Test
	public void testTakeWhite() {
		engineBoard = initializedBoard(WHITE, board);
		int move = createMove("h2g3", engineBoard);
		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				....♚...
				........
				........
				........
				........
				......♙.
				........
				....♔...""");
		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}

	@Test
	public void testTakeBlack() {
		engineBoard = initializedBoard(BLACK, board);
		int move = createMove("g3h2", engineBoard);
		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				....♚...
				........
				........
				........
				........
				........
				.......♟
				....♔...""");
		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}
}
