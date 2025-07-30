package nl.arthurvlug.chess.engine.ace.board

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.ImmutableList
import com.google.common.eventbus.EventBus
import nl.arthurvlug.chess.engine.ColorUtils
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.KingEatingException
import nl.arthurvlug.chess.engine.ace.PieceUtils
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm
import nl.arthurvlug.chess.engine.ace.board.AceBoardDebugUtils.string
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils
import nl.arthurvlug.chess.engine.utils.AceBoardTestUtils
import nl.arthurvlug.chess.utils.MoveUtils
import nl.arthurvlug.chess.utils.board.pieces.Color
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils
import nl.arthurvlug.chess.utils.jackson.JacksonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections
import kotlin.math.min

class ACEBoardTest {
    @BeforeEach
    fun before() {
        MoveUtils.DEBUG = true
    }

    @Test
    fun testUnapplyAfterCastlingPutsRookBack() {
        val engineBoard = ACEBoardUtils.initializedBoard(
            Color.BLACK, """
				....♚..♜
				♟.....♟♟
				.♝..♟...
				...♟....
				♛..♙♞♙..
				♙..♕.♘..
				.♙....♙♙
				.♔...♖..
				
				""".trimIndent()
        )
        val unapplyFlags = AceBoardTestUtils.getUnapplyFlags(engineBoard)
        val dumpBefore = ACEBoardUtils.stringDump(engineBoard)
        val move = UnapplyableMoveUtils.createMove("e8g8", engineBoard)
        engineBoard.apply(move)
        AceBoardTestUtils.unapply(engineBoard, move, unapplyFlags)
        val dumpAfter = ACEBoardUtils.stringDump(engineBoard)

        // Check that the rook moves back to h8 after considering castling king-side
        assertThat(dumpBefore).isEqualTo(dumpAfter)
    }

    private fun createAlgorithm(configuration: AceConfiguration): AlphaBetaPruningAlgorithm {
        val algorithm = AlphaBetaPruningAlgorithm(configuration)
        algorithm.setEventBus(EventBus())
        return algorithm
    }


    // Check ignored after Nd2
    //	[10:04:59:059 BST]  INFO [CustomEngine 27] n.a.c.e.c.CustomEngine: [d2d4, d7d5, b1c3, b8c6, c1f4, c8f5, c3b5, a8c8, g1f3, e7e6, b5c7, c8c7, f4c7, d8c7, e2e3, c7b6, d1c1, c6b4, f1d3, f5d3, e1d2, d3e4, c2c4, b4c2, a1b1, f8b4, d2d1, c2e3, f2e3, e4b1, c4c5, b6a5, c1b1, g8f6, a2a3, a5a4, d1c1, b4a5, b1d3, f6e4, h1f1, a5c7, c1b1, e8f8, d3d1, a4b5, b1a1, f8g8, a3a4, b5c6, a1b1, b7b6, c5b6, c6b6, d1c1, b6b3, f3e5, f7f5, e5f7, g8f7, c1c7, f7g6, f1c1, e4d2, c7g3]
    @Test
    @Throws(Exception::class)
    fun testStartingPosition() {
        verifyBitboards(startPositionBoard)
    }

    @Test
    fun testAfterPlayingMoves() {
        val copyBoard = startPositionBoard.cloneBoard(ColorUtils.BLACK, false)
        copyBoard.apply(ImmutableList.of("f8f7"))
        val blackToMove = copyBoard.cloneBoard(ColorUtils.opponent(copyBoard.toMove), false)
        verifyCopyBoard(blackToMove)
    }

    private fun verifyBitboards(startPositionBoard: ACEBoard) {
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_pawns,
            BitboardUtils.bitboardFromFieldName("a1 f7")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_knights,
            BitboardUtils.bitboardFromFieldName("b1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_bishops,
            BitboardUtils.bitboardFromFieldName("c1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_rooks,
            BitboardUtils.bitboardFromFieldName("d1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_queens,
            BitboardUtils.bitboardFromFieldName("e1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.white_kings,
            BitboardUtils.bitboardFromFieldName("f1")
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_pawns,
            BitboardUtils.bitboardFromFieldName("a8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_knights,
            BitboardUtils.bitboardFromFieldName("b8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_bishops,
            BitboardUtils.bitboardFromFieldName("c8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_rooks,
            BitboardUtils.bitboardFromFieldName("d8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_queens,
            BitboardUtils.bitboardFromFieldName("e8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.black_kings,
            BitboardUtils.bitboardFromFieldName("f8")
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.enemy_and_empty_board, BitboardUtils.bitboardFromBoard(
                """
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟.♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ......♟♟
                
                """.trimIndent()
            )
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.occupiedSquares[ColorUtils.WHITE.toInt()], BitboardUtils.bitboardFromBoard(
                """
                    ........
                    .....♟..
                    ........
                    ........
                    ........
                    ........
                    ........
                    ♟♟♟♟♟♟..
                    
                    """.trimIndent()
            )
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            startPositionBoard.occupiedSquares[ColorUtils.BLACK.toInt()], BitboardUtils.bitboardFromBoard(
                """
                    ♟♟♟♟♟♟..
                    ........
                    ........
                    ........
                    ........
                    ........
                    ........
                    ........
                    
                    """.trimIndent()
            )
        )
    }

    private fun verifyCopyBoard(copyBoard: ACEBoard) {
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.white_pawns, BitboardUtils.bitboardFromFieldName("a1"))
        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.white_knights,
            BitboardUtils.bitboardFromFieldName("b1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.white_bishops,
            BitboardUtils.bitboardFromFieldName("c1")
        )
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.white_rooks, BitboardUtils.bitboardFromFieldName("d1"))
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.white_queens, BitboardUtils.bitboardFromFieldName("e1"))
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.white_kings, BitboardUtils.bitboardFromFieldName("f1"))

        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.black_pawns, BitboardUtils.bitboardFromFieldName("a8"))
        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.black_knights,
            BitboardUtils.bitboardFromFieldName("b8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.black_bishops,
            BitboardUtils.bitboardFromFieldName("c8")
        )
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.black_rooks, BitboardUtils.bitboardFromFieldName("d8"))
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.black_queens, BitboardUtils.bitboardFromFieldName("e8"))
        org.junit.jupiter.api.Assertions.assertEquals(copyBoard.black_kings, BitboardUtils.bitboardFromFieldName("f7"))

        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.enemy_and_empty_board, BitboardUtils.bitboardFromBoard(
                """
                .....♟♟♟
                ♟♟♟♟♟.♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                ♟♟♟♟♟♟♟♟
                
                """.trimIndent()
            )
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.occupiedSquares[ColorUtils.WHITE.toInt()], BitboardUtils.bitboardFromBoard(
                """
                    ........
                    ........
                    ........
                    ........
                    ........
                    ........
                    ........
                    ♟♟♟♟♟♟..
                    
                    """.trimIndent()
            )
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            copyBoard.occupiedSquares[ColorUtils.BLACK.toInt()], BitboardUtils.bitboardFromBoard(
                """
                    ♟♟♟♟♟...
                    .....♟..
                    ........
                    ........
                    ........
                    ........
                    ........
                    ........
                    
                    """.trimIndent()
            )
        )
    }

    @Test
    fun testUnapplyPawnMove() {
        val oldBoard = ACEBoardUtils.initializedBoard(
            Color.WHITE, """
     ♟♞♝♜♛♚..
     .....♙..
     ........
     ........
     ........
     ........
     ........
     ♙♘♗♖♕♔..
     
     """.trimIndent()
        )


        val move = UnapplyableMoveUtils.createMove("f7e8", oldBoard)

        val newBoard = startPositionBoard.cloneBoard()
        val white_king_or_rook_queen_side_moved = newBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = newBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = newBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = newBoard.black_king_or_rook_king_side_moved
        //		boolean incFiftyClock = newBoard.incFiftyClock;
        newBoard.apply(move)
        newBoard.unapply(
            move,
            white_king_or_rook_queen_side_moved,
            white_king_or_rook_king_side_moved,
            black_king_or_rook_queen_side_moved,
            black_king_or_rook_king_side_moved
        )
        org.junit.jupiter.api.Assertions.assertEquals(
            ACEBoardUtils.stringDump(oldBoard),
            ACEBoardUtils.stringDump(newBoard)
        )
    }

    @Test
    @Throws(KingEatingException::class)
    fun testPiecesArray() {
        val board = ACEBoardUtils.initializedBoard(
            Color.WHITE, """
     ♟♞♝♜♛♚..
     .....♙..
     ........
     ........
     ........
     ........
     ........
     ♙♘♗♖♕♔..
     
     """.trimIndent()
        )
        val oldBoard = board.cloneBoard()

        val takeMoves = board.generateTakeMoves()
        assertThat(takeMoves).containsExactlyInAnyOrder(
            UnapplyableMoveUtils.createMove("f7e8q", board),
            UnapplyableMoveUtils.createMove("f7e8r", board),
            UnapplyableMoveUtils.createMove("f7e8b", board),
            UnapplyableMoveUtils.createMove("f7e8n", board),
            UnapplyableMoveUtils.createMove("e1e8", board),
            UnapplyableMoveUtils.createMove("d1d8", board)
        )

        val move = UnapplyableMoveUtils.createMove("d1d8", board)
        board.apply(move)

        board.unapply(move, true, true, true, true)

        assertThat(ACEBoardUtils.stringDump(board)).isEqualTo(ACEBoardUtils.stringDump(oldBoard))
    }

    @Test
    @Throws(Exception::class)
    fun testUnapplyTakeMove() {
        verifyBitboards(startPositionBoard)
    }

    @Test
    fun testPromotion() {
        val board = ACEBoardUtils.initializedBoard(
            Color.BLACK, """
     ......♚.
     ......♟♟
     .♙......
     ....♟...
     .♙......
     ......♜.
     ♔....♟..
     ........
     """.trimIndent()
        )

        //		boolean incFiftyClock = board.incFiftyClock;
        val clonedBoard = board.cloneBoard()
        val move = UnapplyableMoveUtils.createMove("f2f1q", board)
        board.apply(move)

        assertThat(board.string()).isEqualTo(
            """
                ......♚.
                ......♟♟
                .♙......
                ....♟...
                .♙......
                ......♜.
                ♔.......
                .....♛..
                
                """.trimIndent()
        )

        board.unapply(move, true, true, true, true)
        assertThat(ACEBoardUtils.stringDump(clonedBoard)).isEqualTo(ACEBoardUtils.stringDump(board))
    }

    @Test
    fun applyF4G3() {
        val expectedInitialBoard = """
				..♝....♜
				♟.♘.♟♟♝♟
				.♟..♚.♟.
				.♗.♟♙♞..
				.♙...♗..
				♛.♙.♙...
				♙....♙♙♙
				♖..♕♔..♖
				
				""".trimIndent()
        val expectedBoardAfterMoving = """
				..♝....♜
				♟.♘.♟♟♝♟
				.♟..♚.♟.
				.♗.♟♙♞..
				.♙......
				♛.♙.♙.♗.
				♙....♙♙♙
				♖..♕♔..♖
				
				""".trimIndent()
        val initialEngineBoardJson = """
				{
				  "toMove" : 0,
				  "black_kings" : 17592186044416,
				  "white_kings" : 16,
				  "black_queens" : 65536,
				  "white_queens" : 8,
				  "white_rooks" : 129,
				  "black_rooks" : -9223372036854775808,
				  "white_bishops" : 9126805504,
				  "black_bishops" : 306244774661193728,
				  "white_knights" : 1125899906842624,
				  "black_knights" : 137438953472,
				  "white_pawns" : 68754399488,
				  "black_pawns" : 49893673004957696,
				  "occupiedSquares" : [ 1125977788047769, -8867215859563560960 ],
				  "unoccupied_board" : 8866089881775513190,
				  "occupied_board" : -8866089881775513191,
				  "enemy_and_empty_board" : -1125977788047770,
				  "white_king_or_rook_queen_side_moved" : false,
				  "white_king_or_rook_king_side_moved" : false,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : false,
				  "pieces" : [ 4, 0, 0, 5, 6, 0, 0, 4, 1, 0, 0, 0, 0, 1, 1, 1, 11, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 3, 0, 7, 1, 8, 0, 0, 0, 7, 0, 0, 12, 0, 7, 0, 7, 0, 2, 0, 7, 7, 9, 7, 0, 0, 9, 0, 0, 0, 0, 10 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -513536744,
				  "plyStack" : [ 198045 ]
				}
				""".trimIndent()

        val expectedEngineBoardAfterMove = """
				{
				  "toMove" : 1,
				  "black_kings" : 17592186044416,
				  "white_kings" : 16,
				  "black_queens" : 65536,
				  "white_queens" : 8,
				  "white_rooks" : 129,
				  "black_rooks" : -9223372036854775808,
				  "white_bishops" : 8594128896,
				  "black_bishops" : 306244774661193728,
				  "white_knights" : 1125899906842624,
				  "black_knights" : 137438953472,
				  "white_pawns" : 68754399488,
				  "black_pawns" : 49893673004957696,
				  "occupiedSquares" : [ 1125977255371161, -8867215859563560960 ],
				  "unoccupied_board" : 8866089882308189798,
				  "occupied_board" : -8866089882308189799,
				  "enemy_and_empty_board" : 8867215859563560959,
				  "white_king_or_rook_queen_side_moved" : false,
				  "white_king_or_rook_king_side_moved" : false,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : false,
				  "pieces" : [ 4, 0, 0, 5, 6, 0, 0, 4, 1, 0, 0, 0, 0, 1, 1, 1, 11, 0, 1, 0, 1, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 3, 0, 7, 1, 8, 0, 0, 0, 7, 0, 0, 12, 0, 7, 0, 7, 0, 2, 0, 7, 7, 9, 7, 0, 0, 9, 0, 0, 0, 0, 10 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -607014438,
				  "plyStack" : [ 198045, 198045 ]
				}
				""".trimIndent()

        val engineBoard = JacksonUtils.fromJson(initialEngineBoardJson, object : TypeReference<ACEBoard?>() {})!!
        assertThat(engineBoard.string()).isEqualTo(expectedInitialBoard)
        assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(initialEngineBoardJson)

        val white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved

        //		int fiftyMove = engineBoard.getFiftyMove();
//		boolean incFiftyClock = engineBoard.incFiftyClock;
        val engineBoardStringBefore: String = engineBoard.string()

        val move = createMoveForSquares(engineBoard)
        engineBoard.apply(move)

        assertThat(engineBoard.string()).isEqualTo(expectedBoardAfterMoving)

        assertThat(engineBoardStringBefore).isNotEqualTo(expectedEngineBoardAfterMove)
        assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(expectedEngineBoardAfterMove)

        engineBoard.unapply(
            move,
            white_king_or_rook_queen_side_moved,
            white_king_or_rook_king_side_moved,
            black_king_or_rook_queen_side_moved,
            black_king_or_rook_king_side_moved
        )

        assertThat(engineBoard.string()).isEqualTo(engineBoardStringBefore)
        assertThat(JacksonUtils.toJson(engineBoard)).isEqualTo(initialEngineBoardJson)
    }

    @Test
    fun testQueenTakesQueen() {
        val position = """
				♚♜......
				.♟.♟....
				♟.♟.....
				.♟......
				........
				........
				♖.......
				♔.......
				
				""".trimIndent()
        val json = """
				{
				  "toMove" : 0,
				  "black_kings" : 72057594037927936,
				  "white_kings" : 1,
				  "black_queens" : 0,
				  "white_queens" : 0,
				  "white_rooks" : 256,
				  "black_rooks" : 144115188075855872,
				  "white_bishops" : 0,
				  "black_bishops" : 0,
				  "white_knights" : 0,
				  "black_knights" : 0,
				  "white_pawns" : 0,
				  "black_pawns" : 2820255915180032,
				  "occupiedSquares" : [ 257, 218993038028963840 ],
				  "unoccupied_board" : -218993038028964098,
				  "occupied_board" : 218993038028964097,
				  "enemy_and_empty_board" : -258,
				  "white_king_or_rook_queen_side_moved" : true,
				  "white_king_or_rook_king_side_moved" : true,
				  "black_king_or_rook_queen_side_moved" : true,
				  "black_king_or_rook_king_side_moved" : true,
				  "pieces" : [ 6, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 7, 0, 0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 12, 10, 0, 0, 0, 0, 0, 0 ],
				  "repeatedMove" : 0,
				  "zobristHash" : -678140938,
				  "plyStack" : [ ]
				}
				""".trimIndent()
        val aceBoard = JacksonUtils.fromJson(json, object : TypeReference<ACEBoard?>() {})!!

        val json2 = JacksonUtils.toJson(aceBoard)
        assertThat(json2).isEqualTo(json)

        assertThat(aceBoard.string()).isEqualTo(position)

        //		aceBoard.checkConsistency();
        val move = UnapplyableMoveUtils.createMove("b5f1", aceBoard)
        assertThat(move).isEqualTo(459105)

        MoveUtils.DEBUG = false
        val white_king_or_rook_queen_side_moved = aceBoard.white_king_or_rook_queen_side_moved
        val white_king_or_rook_king_side_moved = aceBoard.white_king_or_rook_king_side_moved
        val black_king_or_rook_queen_side_moved = aceBoard.black_king_or_rook_queen_side_moved
        val black_king_or_rook_king_side_moved = aceBoard.black_king_or_rook_king_side_moved

        val expected = ACEBoardUtils.stringDump(aceBoard)

        aceBoard.apply(move)
        aceBoard.unapply(
            move,
            white_king_or_rook_queen_side_moved,
            white_king_or_rook_king_side_moved,
            black_king_or_rook_queen_side_moved,
            black_king_or_rook_king_side_moved
        )

        val actual = ACEBoardUtils.stringDump(aceBoard)
        assertThat(actual).isEqualTo(expected)
    }

    companion object {
        private val startPositionBoard: ACEBoard = ACEBoardUtils.initializedBoard(
            Color.WHITE,
            """
                ♟♞♝♜♛♚..
                .....♙..
                ........
                ........
                ........
                ........
                ........
                ♙♘♗♖♕♔..
                
                """.trimIndent()
        )

        private fun createMoveForSquares(engineBoard: ACEBoard): Int {
            val fromField = "f4"
            val toField = "g3"
            val fromIdx = java.lang.Long.numberOfTrailingZeros(BitboardUtils.bitboardFromFieldName(fromField)).toByte()
            val targetIdx = java.lang.Long.numberOfTrailingZeros(BitboardUtils.bitboardFromFieldName(toField)).toByte()
            val coloredMovingPiece = engineBoard.coloredPiece(fromField).toInt()
            val move = UnapplyableMove.create(
                fromIdx.toInt(),
                targetIdx.toInt(),
                coloredMovingPiece,
                ColoredPieceType.NO_PIECE.toInt(),
                ColoredPieceType.NO_PIECE.toInt()
            )
            return move
        }

        private fun printPieces(engineBoard: ACEBoard): String {
            val array = engineBoard.pieces
            val chunkSize = 8

            val chunks: MutableList<String?> = ArrayList()
            var i = 0
            while (i < array.size) {
                val sb = StringBuilder()
                for (j in i..<min((i + chunkSize).toDouble(), array.size.toDouble()).toInt()) {
                    val piece = PieceUtils.typeOrDot(array[j])
                    val coloredPieceOpt =
                        PieceStringUtils.coloredPieceFromCharacter(piece, PieceStringUtils.pieceToCharacterConverter)
                    if (coloredPieceOpt.isPresent) {
                        val coloredPiece = coloredPieceOpt.get()
                        val pieceSymbol = PieceStringUtils.pieceToChessSymbolMap.map[coloredPiece.pieceType]
                        sb.append(if (coloredPiece.color == Color.WHITE) pieceSymbol!!.white else pieceSymbol!!.black)
                    } else {
                        sb.append(' ')
                    }
                }
                chunks.add("\n%s".formatted(sb))
                i += chunkSize
            }
            Collections.reverse(chunks)
            return chunks.toString()
        }
    }
}
