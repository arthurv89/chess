package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.MoveUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils.createMove;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.initializedBoard;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.piecesToString;
import static nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.stringDump;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.getUnapplyFlags;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.unapply;
import static nl.arthurvlug.chess.utils.board.pieces.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

public class AceBoardCastlingTest {
	private ACEBoard engineBoard;
	private UnapplyFlags unapplyFlags;

	@BeforeEach
    public void before() {
        MoveUtils.DEBUG = true;
		engineBoard = initializedBoard(WHITE, """
				♜...♚..♜
				........
				........
				........
				........
				........
				........
				♖...♔..♖""");
		unapplyFlags = getUnapplyFlags(engineBoard);
    }

	@Test
	public void testCastleQueenSideWhite() {
		int move = createMove("e1c1", engineBoard);
		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				♜...♚..♜
				........
				........
				........
				........
				........
				........
				..♔♖...♖""");
		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}

	@Test
	public void testCastleKingSideWhite() {
		int move = createMove("e1g1", engineBoard);
		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				♜...♚..♜
				........
				........
				........
				........
				........
				........
				♖....♖♔.""");
		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}

	@Test
	public void testCastleQueenSideBlack() {
		int move = createMove("e8c8", engineBoard);
		engineBoard.toMove = opponent(engineBoard.toMove);
		engineBoard.mutateGeneralBoardOccupation();
		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				..♚♜...♜
				........
				........
				........
				........
				........
				........
				♖...♔..♖""");

		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}

	@Test
	public void testCastleKingSideBlack() {
		int move = createMove("e8g8", engineBoard);
		engineBoard.toMove = opponent(engineBoard.toMove);
		engineBoard.mutateGeneralBoardOccupation();

		String stringDumpBefore = stringDump(engineBoard);

		engineBoard.apply(move);
		assertThat(piecesToString(engineBoard.getPieces())).isEqualTo("""
				♜....♜♚.
				........
				........
				........
				........
				........
				........
				♖...♔..♖""");

		unapply(engineBoard, move, unapplyFlags);
		assertThat(stringDump(engineBoard)).isEqualTo(stringDumpBefore);
	}
}
