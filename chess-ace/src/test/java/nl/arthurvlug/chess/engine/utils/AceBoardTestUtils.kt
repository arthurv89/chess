package nl.arthurvlug.chess.engine.utils

import com.google.common.collect.ImmutableList
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils
import nl.arthurvlug.chess.engine.ace.board.ACEBoard
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard
import nl.arthurvlug.chess.engine.ace.board.UnapplyFlags
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.utils.MoveUtils.ENGINE_DEBUG

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

    fun ACEBoard.apply(moveList: List<String>) {
        if (moveList.isEmpty()) {
            return
        }

        for (sMove in moveList) {
            apply(sMove)
        }
    }

    fun ACEBoard.apply(sMove: String) {
        val move = UnapplyableMoveUtils.createMove(sMove, this)
        if (ENGINE_DEBUG) {
            val movingPiece = UnapplyableMove.coloredMovingPiece(move)
            if(movingPiece == ColoredPieceType.NO_PIECE) {
                throw RuntimeException("Could not determine moving piece while executing " + UnapplyableMoveUtils.toString(move))
            }
        }
        apply(move)
    }

    @JvmStatic
    fun ACEBoard.unapply(move: Int, unapplyFlags: UnapplyFlags) {
        unapply(
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
