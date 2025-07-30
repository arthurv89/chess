package nl.arthurvlug.chess.engine.utils;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.UnapplyFlags;

public class AceBoardTestUtils {
    public static UnapplyFlags getUnapplyFlags(ACEBoard engineBoard) {
        return new UnapplyFlags(engineBoard.white_king_or_rook_queen_side_moved,
                engineBoard.white_king_or_rook_king_side_moved,
                engineBoard.black_king_or_rook_queen_side_moved,
                engineBoard.black_king_or_rook_king_side_moved);
    }

    public static void unapply(ACEBoard engineBoard, int move, UnapplyFlags unapplyFlags) {
        engineBoard.unapply(move,
                unapplyFlags.whiteKingOrRookQueenSideMoved(),
                unapplyFlags.whiteKingOrRookKingSideMoved(),
                unapplyFlags.blackKingOrRookQueenSideMoved(),
                unapplyFlags.blackKingOrRookKingSideMoved());
    }
}
