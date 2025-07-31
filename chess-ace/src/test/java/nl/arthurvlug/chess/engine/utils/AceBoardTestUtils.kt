package nl.arthurvlug.chess.engine.utils

import com.google.common.collect.ImmutableList
import nl.arthurvlug.chess.engine.ace.board.ACEBoard
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard
import nl.arthurvlug.chess.engine.ace.board.UnapplyFlags
import nl.arthurvlug.chess.utils.MoveUtils

object AceBoardTestUtils {
    @JvmField
    var defaultUnapplyFlags: UnapplyFlags = UnapplyFlags(
        true,
        true,
        true,
        true,
        0
    )

    @JvmStatic
    fun getUnapplyFlags(engineBoard: ACEBoard): UnapplyFlags {
        return UnapplyFlags(
            engineBoard.white_king_or_rook_queen_side_moved,
            engineBoard.white_king_or_rook_king_side_moved,
            engineBoard.black_king_or_rook_queen_side_moved,
            engineBoard.black_king_or_rook_king_side_moved,
            engineBoard.fiftyMove
        )
    }

    @JvmStatic
    fun unapply(engineBoard: ACEBoard, move: Int, unapplyFlags: UnapplyFlags) {
        engineBoard.unapply(
            move,
            unapplyFlags.whiteKingOrRookQueenSideMoved,
            unapplyFlags.whiteKingOrRookKingSideMoved,
            unapplyFlags.blackKingOrRookQueenSideMoved,
            unapplyFlags.blackKingOrRookKingSideMoved,
            unapplyFlags.fiftyMove
        )
    }

    @JvmStatic
    fun createEngineBoardForMoves(moves: List<String>): ACEBoard {
        val engineBoard: ACEBoard = InitialACEBoard.createInitialACEBoard()
        engineBoard.apply(moves)
        return engineBoard
    }

    @JvmStatic
    fun createEngineBoard(movesString: String): ACEBoard {
        val moves: List<String> = ImmutableList.copyOf(movesString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return createEngineBoardForMoves(moves)
    }
}
