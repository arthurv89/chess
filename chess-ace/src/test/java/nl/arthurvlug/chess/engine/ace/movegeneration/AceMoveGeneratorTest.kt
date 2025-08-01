package nl.arthurvlug.chess.engine.ace.movegeneration

import com.google.common.collect.ImmutableList
import nl.arthurvlug.chess.engine.ColorUtils
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.KingEatingException
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils
import nl.arthurvlug.chess.engine.ace.board.ACEBoard
import nl.arthurvlug.chess.engine.ace.board.ACEBoard.Companion.emptyBoard
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard
import nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.apply
import nl.arthurvlug.chess.utils.board.FieldUtils
import nl.arthurvlug.chess.utils.board.pieces.Color
import nl.arthurvlug.chess.utils.board.pieces.PieceType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class AceMoveGeneratorTest {
    @Test
    @Throws(Exception::class)
    fun testKingMoves() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, false)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("b7"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("a7"))
        engineBoard.finalizeBitboards()

        val whiteMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false))
        Assertions.assertEquals(3, whiteMoves.size)
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a2", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b2", engineBoard)))

        val blackMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false))
        Assertions.assertEquals(9, blackMoves.size)
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a7a5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a7a6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7b8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7b6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c6", engineBoard)))
    }

    @Test
    @Throws(Exception::class)
    fun testCastlingMovesWhite() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("h1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.finalizeBitboards()

        val moves = AceMoveGenerator.castlingMoves(engineBoard)
        Assertions.assertEquals(2, moves.size)
    }

    @Test
    @Throws(Exception::class)
    fun testCastlingMovesWhite_rook_kingside_moved() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("h1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.finalizeBitboards()

        engineBoard.apply(listOf("h1h8", "e8e7", "h8h1", "e7e8"))

        val moves = AceMoveGenerator.castlingMoves(engineBoard)
        Assertions.assertEquals(1, moves.size)
    }

    @Test
    @Throws(Exception::class)
    fun testCastling_when_white_castled_kingside_then_verify_king_and_rook_position() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("h1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.finalizeBitboards()

        engineBoard.apply(ImmutableList.of("e1g1"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("e1"))
        Assertions.assertEquals(ColoredPieceType.WHITE_ROOK_BYTE, engineBoard.coloredPiece("f1"))
        Assertions.assertEquals(ColoredPieceType.WHITE_KING_BYTE, engineBoard.coloredPiece("g1"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("h1"))
    }

    @Test
    @Throws(Exception::class)
    fun testCastling_when_white_castled_queenside_then_verify_king_and_rook_position() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("h1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.finalizeBitboards()

        engineBoard.apply(ImmutableList.of("e1c1"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("e1"))
        Assertions.assertEquals(ColoredPieceType.WHITE_KING_BYTE, engineBoard.coloredPiece("c1"))
        Assertions.assertEquals(ColoredPieceType.WHITE_ROOK_BYTE, engineBoard.coloredPiece("d1"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("a1"))
    }

    @Test
    @Throws(Exception::class)
    fun testCastlingMovesBlack() {
        val engineBoard = emptyBoard(ColorUtils.BLACK, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("a8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("h8"))
        engineBoard.finalizeBitboards()

        val moves = AceMoveGenerator.castlingMoves(engineBoard)
        Assertions.assertEquals(2, moves.size)
    }

    @Test
    @Throws(Exception::class)
    fun testCastling_when_black_castled_kingside_then_verify_king_and_rook_position() {
        val engineBoard = emptyBoard(ColorUtils.BLACK, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("a8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("h8"))
        engineBoard.finalizeBitboards()

        engineBoard.apply(ImmutableList.of("e8g8"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("e8"))
        Assertions.assertEquals(ColoredPieceType.BLACK_ROOK_BYTE, engineBoard.coloredPiece("f8"))
        Assertions.assertEquals(ColoredPieceType.BLACK_KING_BYTE, engineBoard.coloredPiece("g8"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("h8"))
    }

    @Test
    @Throws(Exception::class)
    fun testCastling_when_black_castled_queenside_then_verify_king_and_rook_position() {
        val engineBoard = emptyBoard(ColorUtils.BLACK, true)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("a8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("h8"))
        engineBoard.finalizeBitboards()

        engineBoard.apply(ImmutableList.of("e8c8"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("e8"))
        Assertions.assertEquals(ColoredPieceType.BLACK_KING_BYTE, engineBoard.coloredPiece("c8"))
        Assertions.assertEquals(ColoredPieceType.BLACK_ROOK_BYTE, engineBoard.coloredPiece("d8"))
        Assertions.assertEquals(ColoredPieceType.NO_PIECE, engineBoard.coloredPiece("a8"))
    }

    @Test
    @Throws(KingEatingException::class)
    fun testCastling_when_passing_attacked_field_then_dont_allow_castling() {
        val moves: List<String> = ImmutableList.copyOf(
            "e2e4 d7d5 e4d5 g8f6 d2d4 f6d5 g1f3 b8c6 f1b5 e7e6 e1g1 f8d6 c2c4 d5f6 d4d5 e6d5 c4d5 d6h2 f3h2 f6d5 f1e1 c8e6 d1c2 d5b4 c2c5".split(
                " ".toRegex()
            ).dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val engineBoard = createEngineBoard(moves)
        val x = AceMoveGenerator.castlingMoves(engineBoard)
        org.assertj.core.api.Assertions.assertThat(UnapplyableMoveUtils.listToString(x)).hasSize(0)
    }

    private fun createEngineBoard(moves: List<String>): ACEBoard {
        val engineBoard: ACEBoard = InitialACEBoard.createInitialACEBoard()
        engineBoard.apply(moves)
        return engineBoard
    }

    @Test
    @Throws(Exception::class)
    fun testRookMoves() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, false)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("h8"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("a4"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("h6"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("b2"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("f1"))
        engineBoard.finalizeBitboards()

        val whiteMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false))
        Assertions.assertEquals(11, whiteMoves.size)
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a4a5", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a2", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1a3", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1c1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1d1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1e1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1f1", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)))
        // Illegal createMoves
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)))

        val blackMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false))
        Assertions.assertEquals(19, blackMoves.size)
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b1", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b3", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b4", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2b8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2a2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2c2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2e2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2f2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2g2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2h2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)))
        // Illegal createMoves
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)))
    }

    @Test
    @Throws(Exception::class)
    fun testKnightMoves() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, false)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("h8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("h6"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KNIGHT, FieldUtils.fieldIdx("a1"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KNIGHT, FieldUtils.fieldIdx("b2"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("c4"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("a4"))
        engineBoard.finalizeBitboards()

        val whiteMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false))
        Assertions.assertEquals(6, whiteMoves.size)
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a4a5", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1b3", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("a1c2", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)))

        val blackMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false))
        Assertions.assertEquals(9, blackMoves.size)
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("c4c3", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2a4", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d3", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b2d1", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)))
    }

    @Test
    @Throws(Exception::class)
    fun testBishopMoves() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, false)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.BISHOP, FieldUtils.fieldIdx("d1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c2"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("d2"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("e2"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.BISHOP, FieldUtils.fieldIdx("b7"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("c6"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c5"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("h8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("h6"))
        engineBoard.finalizeBitboards()

        val whiteMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE, false))
        Assertions.assertEquals(9, whiteMoves.size)
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g8", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("e2e3", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("e2e4", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("d2d3", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("d2d4", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("c2c3", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("c2c4", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)))
        Assertions.assertTrue(whiteMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)))

        val blackMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false))
        Assertions.assertEquals(8, blackMoves.size)
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7c8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("b7a6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6h7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h6g7", engineBoard)))
    }

    @Test
    @Throws(Exception::class)
    fun testQueenMoves() {
        val engineBoard = emptyBoard(ColorUtils.WHITE, false)
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.KING, FieldUtils.fieldIdx("h6"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.QUEEN, FieldUtils.fieldIdx("d1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("e1"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c2"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("d2"))
        engineBoard.addPiece(ColorUtils.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("e2"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.KING, FieldUtils.fieldIdx("h8"))
        engineBoard.addPiece(ColorUtils.BLACK, PieceType.QUEEN, FieldUtils.fieldIdx("a8"))
        engineBoard.finalizeBitboards()


//		List<Integer> whiteMoves = EngineTestUtils.AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.WHITE));
//		assertEquals(0, whiteMoves.size());
        val blackMoves = AceMoveGenerator.generateMoves(engineBoard.cloneBoard(ColorUtils.BLACK, false))
        Assertions.assertEquals(23, blackMoves.size)
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a4", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a3", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8a1", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8b8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8c8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8d8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8e8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8f8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8g8", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8b7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8c6", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8d5", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8e4", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8f3", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8g2", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("a8h1", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h8h7", engineBoard)))
        Assertions.assertTrue(blackMoves.contains(UnapplyableMoveUtils.createMove("h8g7", engineBoard)))
    }

    @Test
    @Disabled
    fun testShouldNotCastle() {
        val board = ACEBoardUtils.initializedBoard(
            Color.BLACK, """
     ♜..♛♚..♜
     ♟.♟♗.♟♟♟
     ....♟...
     ..♝.♙...
     ........
     ....♙♕..
     .♙...♙♙♙
     ♖.♗..♖♔.
     
     """.trimIndent()
        )
    }
}
