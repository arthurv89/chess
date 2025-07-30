package nl.arthurvlug.chess.engine.ace.board

import com.google.common.base.Joiner
import com.google.common.base.Preconditions
import com.google.common.base.Predicate
import com.google.common.base.Predicates
import com.google.common.base.Splitter
import com.google.common.collect.Lists
import nl.arthurvlug.chess.engine.ColorUtils.BLACK
import nl.arthurvlug.chess.engine.ColorUtils.WHITE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_BISHOP_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_KING_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_KNIGHT_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_PAWN_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_QUEEN_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_ROOK_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_BISHOP_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_KING_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_KNIGHT_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_PAWN_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_QUEEN_BYTE
import nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_ROOK_BYTE
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils
import java.util.Collections

object AceBoardDebugUtils {
    fun ACEBoard.checkConsistency() {
        Preconditions.checkArgument((white_pawns and white_knights and white_bishops and white_rooks and white_queens and white_kings) == 0L)
        Preconditions.checkArgument((black_pawns and black_knights and black_bishops and black_rooks and black_queens and black_kings) == 0L)
        val intersectBoard = occupiedSquares[WHITE.toInt()] and occupiedSquares[BLACK.toInt()]
        if(intersectBoard != 0L) {
            throw IllegalArgumentException("""
                    White and black occupy the same fields.

                    Offending field:
${BitboardUtils.toString(intersectBoard)}
                    White:
${whiteBoard()}
                    Black:
${blackBoard()}
                    """)
        }
    }

    private fun ACEBoard.whiteBoard(): String {
        val pieces = listOf(WHITE_PAWN_BYTE, WHITE_KNIGHT_BYTE, WHITE_BISHOP_BYTE, WHITE_ROOK_BYTE, WHITE_QUEEN_BYTE, WHITE_KING_BYTE);
        return string(pieces::contains);
    }

    private fun ACEBoard.blackBoard(): String {
        val pieces = listOf(BLACK_PAWN_BYTE, BLACK_KNIGHT_BYTE, BLACK_BISHOP_BYTE, BLACK_ROOK_BYTE, BLACK_QUEEN_BYTE, BLACK_KING_BYTE);
        return string(pieces::contains);
    }


    fun ACEBoard.string(): String {
        checkConsistency()
        return string(Predicates.alwaysTrue())
    }

    fun ACEBoard.string(predicate: Predicate<Byte>): String {
        val sb = StringBuilder()
        for (fieldIdx in 0..63) {
            if ((fieldIdx) % 8 == 0) {
                sb.append('\n')
            }

            val coloredPieceOnField: Byte = coloredPiece(fieldIdx.toByte())
            if (coloredPieceOnField == ColoredPieceType.NO_PIECE || !predicate.apply(coloredPieceOnField)) {
                sb.append('.')
            } else {
                val c = PieceStringUtils.toCharacterString(
                    ColoredPieceType.from(coloredPieceOnField),
                    PieceStringUtils.pieceToChessSymbolMap
                )
                sb.append(c)
            }
        }
        val reversedBoard = sb.toString()

        // Reverse rows
        val l: List<String?> = Lists.newArrayList(Splitter.on('\n').split(reversedBoard))
        Collections.reverse(l)
        return Joiner.on('\n').join(l)
    }
}
