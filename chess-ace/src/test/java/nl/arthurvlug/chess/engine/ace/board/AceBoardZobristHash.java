package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import org.junit.jupiter.api.Test;

import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.defaultUnapplyFlags;
import static nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.unapply;
import static org.assertj.core.api.Assertions.assertThat;

public class AceBoardZobristHash {
    final ACEBoard engineBoard = ACEBoardUtils.initializedBoard(Color.WHITE, """
                .....♔..
                .....♟♙♟
                ......♟♚
                ......♟♟
                ........
                ........
                ........
                ........
                """);

    @Test
    public void testHash() {
        String before = ACEBoardUtils.stringDump(engineBoard);

        int move1 = UnapplyableMoveUtils.createMove("g7g8n", engineBoard);
        engineBoard.apply(move1);
        int hash1 = engineBoard.getZobristHash();
        UnapplyFlags flags = defaultUnapplyFlags;
        unapply(engineBoard, move1, flags);

        String after = ACEBoardUtils.stringDump(engineBoard);
        assertThat(before).isEqualTo(after);

        int move2 = UnapplyableMoveUtils.createMove("g7g8q", engineBoard);
        engineBoard.apply(move2);
        int hash2 = engineBoard.getZobristHash();

        assertThat(hash1).isNotEqualTo(hash2);

    }
}
