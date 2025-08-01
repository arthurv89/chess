package nl.arthurvlug.chess.engine.ace.evaluation

import com.google.common.collect.ImmutableList
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard
import nl.arthurvlug.chess.engine.utils.AceBoardTestUtils.apply
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AceEvaluatorTest {
    private var aceEvaluator: AceEvaluator? = null

    @BeforeEach
    fun before() {
        this.aceEvaluator = AceEvaluator()
    }

    @Test
    fun testStartingPosition() {
        val board = InitialACEBoard.createInitialACEBoard()
        board.finalizeBitboards()
        val evaluation = aceEvaluator!!.evaluate(board)
        Assertions.assertEquals(0, evaluation)
    }

    @Test
    fun testAfterE4() {
        val board = InitialACEBoard.createInitialACEBoard()
        board.apply(mutableListOf("e2e4"))
        val evaluation = aceEvaluator!!.evaluate(board)
        Assertions.assertEquals(40, evaluation)
    }

    @Test
    fun testRepetition() {
        val board = InitialACEBoard.createInitialACEBoard()
        board.apply(listOf("b1d8", "b8c6", "d8b1", "c6b8", "b1d8", "b8c6", "d8b1", "c6b8", "b1d8"))
        val evaluation = aceEvaluator!!.evaluate(board)
        Assertions.assertEquals(0, evaluation)
    }

    @Test
    fun testRepetition2() {
        val board = InitialACEBoard.createInitialACEBoard()
        board.apply(
            listOf(
                "d2d4",
                "d7d5",
                "b1c3",
                "b8c6",
                "g1f3",
                "g8f6",
                "e2e3",
                "c8f5",
                "c1d2",
                "e7e6",
                "f1d3",
                "f5d3",
                "c2d3",
                "f8d6",
                "d1b3",
                "a8b8",
                "e1g1",
                "e8g8",
                "e3e4",
                "c6b4",
                "e4e5",
                "b4d3",
                "e5d6",
                "d8d6",
                "c3b5",
                "d3c5",
                "b3c2",
                "d6e7",
                "c2c5",
                "e7c5",
                "d4c5",
                "c7c6",
                "b5d4",
                "f6e4",
                "d2f4",
                "b8a8",
                "a1c1",
                "a8c8",
                "f3e5",
                "c8a8",
                "c1c2",
                "a8c8",
                "f1a1",
                "c8a8",
                "a1b1",
                "a8c8",
                "b1a1",
                "c8a8",
                "a1b1",
                "a8c8",
                "b1a1",
                "c8a8",
                "a1b1",
                "a8c8",
                "b1a1"
            )
        )
        val evaluation = aceEvaluator!!.evaluate(board)
        Assertions.assertEquals(0, evaluation)
    }
}
