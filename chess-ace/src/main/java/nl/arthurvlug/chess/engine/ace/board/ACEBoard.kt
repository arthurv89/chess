package nl.arthurvlug.chess.engine.ace.board

import nl.arthurvlug.chess.engine.ColorUtils
import nl.arthurvlug.chess.engine.ace.ColoredPieceType
import nl.arthurvlug.chess.engine.ace.KingEatingException
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.piecesToString
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils.stringDump
import nl.arthurvlug.chess.engine.ace.board.AceBoardDebugUtils.checkConsistency
import nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.*
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator
import nl.arthurvlug.chess.engine.ace.movegeneration.AceTakeMoveGenerator
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils
import nl.arthurvlug.chess.utils.MoveUtils.ENGINE_DEBUG
import nl.arthurvlug.chess.utils.board.FieldUtils
import nl.arthurvlug.chess.utils.board.pieces.PieceType
import nl.arthurvlug.chess.utils.board.pieces.PieceTypeBytes
import java.lang.Long.numberOfTrailingZeros
import java.util.Random
import java.util.Stack

open class ACEBoard protected constructor() {
    @JvmField
    var toMove: Byte = 0

    //	public long[][] pieces = new long[PieceType.values().length][2];
    @JvmField
    var black_kings: Long = 0
    @JvmField
    var white_kings: Long = 0
    @JvmField
    var black_queens: Long = 0
    @JvmField
    var white_queens: Long = 0
    @JvmField
    var white_rooks: Long = 0
    @JvmField
    var black_rooks: Long = 0
    @JvmField
    var white_bishops: Long = 0
    @JvmField
    var black_bishops: Long = 0
    @JvmField
    var white_knights: Long = 0
    @JvmField
    var black_knights: Long = 0
    @JvmField
    var white_pawns: Long = 0
    @JvmField
    var black_pawns: Long = 0

    @JvmField
    var occupiedSquares: LongArray = LongArray(2)
    @JvmField
    var unoccupied_board: Long = 0
    @JvmField
    var occupied_board: Long = 0
    @JvmField
    var enemy_and_empty_board: Long = 0

    // For castling
    @JvmField
    var white_king_or_rook_queen_side_moved: Boolean = false
    @JvmField
    var white_king_or_rook_king_side_moved: Boolean = false
    @JvmField
    var black_king_or_rook_queen_side_moved: Boolean = false
    @JvmField
    var black_king_or_rook_king_side_moved: Boolean = false

    var pieces: ByteArray = ByteArray(64)
        private set

    //	// TODO: Implement
    var fiftyMove: Int = 0;
    // TODO: Implement
    val repeatedMove: Int = 0

    var zobristHash: Int = 0
        private set

    @JvmField
    var plyStack: Stack<Int> = Stack()


    //	public boolean incFiftyClock;
    init {
        val random = Random(1)
        for (fieldIndex in 0..63) {
            for (pieceType in PieceType.entries) {
                for (color in 0..1) {
                    val coloredByte = ColoredPieceType.getColoredByte(pieceType, color.toByte())
                    val rand = random.nextInt()
                    zobristRandomTable[fieldIndex][coloredByte.toInt()] = rand
                }
            }
        }
    }

    fun coloredPiece(fieldName: String?): Byte {
        return coloredPiece(FieldUtils.fieldIdx(fieldName))
    }

    fun coloredPiece(fieldIdx: Byte): Byte {
        val bitboard = BitboardUtils.bitboardFromFieldIdx(fieldIdx)

        if ((white_pawns and bitboard) != 0L) return ColoredPieceType.WHITE_PAWN_BYTE
        if ((white_knights and bitboard) != 0L) return ColoredPieceType.WHITE_KNIGHT_BYTE
        if ((white_bishops and bitboard) != 0L) return ColoredPieceType.WHITE_BISHOP_BYTE
        if ((white_rooks and bitboard) != 0L) return ColoredPieceType.WHITE_ROOK_BYTE
        if ((white_queens and bitboard) != 0L) return ColoredPieceType.WHITE_QUEEN_BYTE
        if ((white_kings and bitboard) != 0L) return ColoredPieceType.WHITE_KING_BYTE

        if ((black_pawns and bitboard) != 0L) return ColoredPieceType.BLACK_PAWN_BYTE
        if ((black_knights and bitboard) != 0L) return ColoredPieceType.BLACK_KNIGHT_BYTE
        if ((black_bishops and bitboard) != 0L) return ColoredPieceType.BLACK_BISHOP_BYTE
        if ((black_rooks and bitboard) != 0L) return ColoredPieceType.BLACK_ROOK_BYTE
        if ((black_queens and bitboard) != 0L) return ColoredPieceType.BLACK_QUEEN_BYTE
        if ((black_kings and bitboard) != 0L) return ColoredPieceType.BLACK_KING_BYTE

        return ColoredPieceType.NO_PIECE
    }

    fun pieceType(fieldIdx: Byte): Int {
        val bitboard = BitboardUtils.bitboardFromFieldIdx(fieldIdx)

        if ((white_pawns and bitboard) != 0L) return PieceTypeBytes.PAWN_BYTE.toInt()
        if ((white_knights and bitboard) != 0L) return PieceTypeBytes.KNIGHT_BYTE.toInt()
        if ((white_bishops and bitboard) != 0L) return PieceTypeBytes.BISHOP_BYTE.toInt()
        if ((white_rooks and bitboard) != 0L) return PieceTypeBytes.ROOK_BYTE.toInt()
        if ((white_queens and bitboard) != 0L) return PieceTypeBytes.QUEEN_BYTE.toInt()
        if ((white_kings and bitboard) != 0L) return PieceTypeBytes.KING_BYTE.toInt()

        if ((black_pawns and bitboard) != 0L) return PieceTypeBytes.PAWN_BYTE.toInt()
        if ((black_knights and bitboard) != 0L) return PieceTypeBytes.KNIGHT_BYTE.toInt()
        if ((black_bishops and bitboard) != 0L) return PieceTypeBytes.BISHOP_BYTE.toInt()
        if ((black_rooks and bitboard) != 0L) return PieceTypeBytes.ROOK_BYTE.toInt()
        if ((black_queens and bitboard) != 0L) return PieceTypeBytes.QUEEN_BYTE.toInt()
        if ((black_kings and bitboard) != 0L) return PieceTypeBytes.KING_BYTE.toInt()

        return ColoredPieceType.NO_PIECE.toInt()
    }

    fun apply(move: Int) {
//		incFiftyClock = true;
        plyStack.push(move)
        // TODO: Fix this by creating a more efficient Move object
        ENGINE_DEBUG && breakpoint()
        val fromIdx = UnapplyableMove.fromIdx(move)
        val fromBitboard = BitboardUtils.bitboardFromFieldIdx(fromIdx)
        ENGINE_DEBUG && breakpoint()

        val targetIdx = UnapplyableMove.targetIdx(move)
        val targetBitboard = BitboardUtils.bitboardFromFieldIdx(targetIdx)

        val coloredMovingPiece = UnapplyableMove.coloredMovingPiece(move)

        val x = 1
        ENGINE_DEBUG && breakpoint()
        pieces[fromIdx.toInt()] = ColoredPieceType.NO_PIECE
        ENGINE_DEBUG && breakpoint()
        pieces[targetIdx.toInt()] = coloredMovingPiece

        ENGINE_DEBUG && breakpoint()
        xorMove(targetBitboard, fromBitboard, coloredMovingPiece, move, true)
        ENGINE_DEBUG && breakpoint()
        xorTakePiece(move, targetBitboard, targetIdx.toInt(), true)
        ENGINE_DEBUG && breakpoint()

        //		if(incFiftyClock) {
        	fiftyMove++;
//		} else {
//			fiftyMove = 0;
//		}
        toMove = ColorUtils.opponent(toMove)
        ENGINE_DEBUG && breakpoint()

        finalizeBitboardsAfterApply(
            fromIdx.toInt(),
            targetIdx.toInt(),
            coloredMovingPiece,
            UnapplyableMove.takePiece(move),
            UnapplyableMove.promotionPiece(move)
        )
        ENGINE_DEBUG && breakpoint()

        if (ENGINE_DEBUG) {
            checkConsistency()
        }
    }

    fun unapply(
        move: Int,
        white_king_or_rook_queen_side_moved_before: Boolean,
        white_king_or_rook_king_side_moved_before: Boolean,
        black_king_or_rook_queen_side_moved_before: Boolean,
        black_king_or_rook_king_side_moved_before: Boolean,
        fiftyMoveBefore: Int
    ) {
        plyStack.pop()
        toMove = ColorUtils.opponent(toMove)

        val targetIdx = UnapplyableMove.targetIdx(move)
        val targetBitboard = BitboardUtils.bitboardFromFieldIdx(targetIdx)
        val coloredMovingPiece = UnapplyableMove.coloredMovingPiece(move)

        val fromIdx = UnapplyableMove.fromIdx(move)
        val fromBitboard = BitboardUtils.bitboardFromFieldIdx(fromIdx)

        // This is a reverse move
        ENGINE_DEBUG && breakpoint()
        pieces[fromIdx.toInt()] = coloredMovingPiece
        ENGINE_DEBUG && breakpoint()

        xorMove(fromBitboard, targetBitboard, coloredMovingPiece, move, false)
        ENGINE_DEBUG && breakpoint()
        xorTakePiece(move, targetBitboard, targetIdx.toInt(), false)
        ENGINE_DEBUG && breakpoint()

        this.white_king_or_rook_queen_side_moved = white_king_or_rook_queen_side_moved_before
        this.white_king_or_rook_king_side_moved = white_king_or_rook_king_side_moved_before
        this.black_king_or_rook_queen_side_moved = black_king_or_rook_queen_side_moved_before
        this.black_king_or_rook_king_side_moved = black_king_or_rook_king_side_moved_before
        this.fiftyMove = fiftyMoveBefore

        finalizeBitboardsAfterApply(
            fromIdx.toInt(),
            targetIdx.toInt(),
            coloredMovingPiece,
            UnapplyableMove.takePiece(move),
            UnapplyableMove.promotionPiece(move)
        )
        ENGINE_DEBUG && breakpoint()

        if (ENGINE_DEBUG) {
            checkConsistency()
        }
    }

    private fun xorMove(
        targetBitboard: Long,
        fromBitboard: Long,
        coloredMovingPiece: Byte,
        move: Int,
        isApply: Boolean
    ) {
        // TODO: Remove if statement
        if (ColorUtils.isWhite(toMove.toInt())) {
            when (coloredMovingPiece) {
                ColoredPieceType.WHITE_PAWN_BYTE -> moveWhitePawn(fromBitboard, targetBitboard, move, isApply)
                ColoredPieceType.WHITE_KNIGHT_BYTE -> moveWhiteKnight(fromBitboard, targetBitboard)
                ColoredPieceType.WHITE_BISHOP_BYTE -> moveWhiteBishop(fromBitboard, targetBitboard)
                ColoredPieceType.WHITE_ROOK_BYTE -> moveWhiteRook(fromBitboard, targetBitboard)
                ColoredPieceType.WHITE_QUEEN_BYTE -> moveWhiteQueen(fromBitboard, targetBitboard)
                ColoredPieceType.WHITE_KING_BYTE -> moveWhiteKing(fromBitboard, targetBitboard, isApply)
            }
            recalculateWhiteOccupiedSquares()
        } else {
            when (coloredMovingPiece) {
                ColoredPieceType.BLACK_PAWN_BYTE -> moveBlackPawn(fromBitboard, targetBitboard, move, isApply)
                ColoredPieceType.BLACK_KNIGHT_BYTE -> moveBlackKnight(fromBitboard, targetBitboard)
                ColoredPieceType.BLACK_BISHOP_BYTE -> moveBlackBishop(fromBitboard, targetBitboard)
                ColoredPieceType.BLACK_ROOK_BYTE -> moveBlackRook(fromBitboard, targetBitboard)
                ColoredPieceType.BLACK_QUEEN_BYTE -> moveBlackQueen(fromBitboard, targetBitboard)
                ColoredPieceType.BLACK_KING_BYTE -> moveBlackKing(fromBitboard, targetBitboard, isApply)
            }
            recalculateBlackOccupiedSquares()
        }
    }

    private fun recalculateWhiteOccupiedSquares() {
        occupiedSquares[ColorUtils.WHITE.toInt()] =
            white_pawns or white_knights or white_bishops or white_rooks or white_queens or white_kings
    }

    private fun recalculateBlackOccupiedSquares() {
        occupiedSquares[ColorUtils.BLACK.toInt()] =
            black_pawns or black_knights or black_bishops or black_rooks or black_queens or black_kings
    }

    private fun xorTakePiece(move: Int, targetBitboard: Long, targetIdx: Int, isApply: Boolean) {
        ENGINE_DEBUG && breakpoint()
        val takePiece = UnapplyableMove.takePiece(move)
        if(!isApply) {
            pieces[targetIdx] = takePiece
        }
        if (ColorUtils.isWhite(toMove.toInt())) {
            when (takePiece) {
                ColoredPieceType.NO_PIECE -> return
                ColoredPieceType.BLACK_PAWN_BYTE -> {
                    black_pawns = black_pawns xor targetBitboard
                }

                ColoredPieceType.BLACK_KNIGHT_BYTE -> {
                    black_knights = black_knights xor targetBitboard
                }

                ColoredPieceType.BLACK_BISHOP_BYTE -> {
                    black_bishops = black_bishops xor targetBitboard
                }

                ColoredPieceType.BLACK_ROOK_BYTE -> {
                    takeBlackRook(targetBitboard)
                }

                ColoredPieceType.BLACK_QUEEN_BYTE -> {
                    black_queens = black_queens xor targetBitboard
                }

                ColoredPieceType.BLACK_KING_BYTE -> {
                    black_kings = black_kings xor targetBitboard
                }
            }
            // TODO: Move this so we don't have to do the if statement
            recalculateBlackOccupiedSquares()
        } else {
            when (takePiece) {
                ColoredPieceType.NO_PIECE -> return
                ColoredPieceType.WHITE_PAWN_BYTE -> {
                    white_pawns = white_pawns xor targetBitboard
                }

                ColoredPieceType.WHITE_KNIGHT_BYTE -> {
                    white_knights = white_knights xor targetBitboard
                }

                ColoredPieceType.WHITE_BISHOP_BYTE -> {
                    white_bishops = white_bishops xor targetBitboard
                }

                ColoredPieceType.WHITE_ROOK_BYTE -> {
                    takeWhiteRook(targetBitboard)
                }

                ColoredPieceType.WHITE_QUEEN_BYTE -> {
                    white_queens = white_queens xor targetBitboard
                }

                ColoredPieceType.WHITE_KING_BYTE -> {
                    white_kings = white_kings xor targetBitboard
                }
            }
            recalculateWhiteOccupiedSquares()
        }
        ENGINE_DEBUG && breakpoint()
    }

    private fun takeWhiteRook(targetBitboard: Long) {
        if (targetBitboard == a1Bitboard) {
            white_king_or_rook_queen_side_moved = true
        } else if (targetBitboard == h1Bitboard) {
            white_king_or_rook_king_side_moved = true
        }
        white_rooks = white_rooks xor targetBitboard
    }

    private fun takeBlackRook(targetBitboard: Long) {
        if (targetBitboard == a8Bitboard) {
            black_king_or_rook_queen_side_moved = true
        } else if (targetBitboard == h8Bitboard) {
            black_king_or_rook_king_side_moved = true
        }
        black_rooks = black_rooks xor targetBitboard
    }

    private fun moveWhitePawn(fromBitboard: Long, targetBitboard: Long, move: Int, isApply: Boolean) {
        if (isApply) {
            if ((targetBitboard and last_row) != 0L) {
                val promotionPiece = UnapplyableMove.promotionPiece(move)
                pieces[numberOfTrailingZeros(fromBitboard)] = ColoredPieceType.NO_PIECE
                pieces[numberOfTrailingZeros(targetBitboard)] = promotionPiece
                promoteWhitePawn(targetBitboard, promotionPiece)
            } else {
                white_pawns = white_pawns xor targetBitboard
            }
            white_pawns = white_pawns xor fromBitboard
        } else {
            if ((fromBitboard and last_row) != 0L) {
                val promotionPiece = UnapplyableMove.promotionPiece(move)
                pieces[numberOfTrailingZeros(fromBitboard)] = ColoredPieceType.NO_PIECE
                pieces[numberOfTrailingZeros(targetBitboard)] = ColoredPieceType.WHITE_PAWN_BYTE
                promoteWhitePawn(fromBitboard, promotionPiece)
            } else {
                white_pawns = white_pawns xor fromBitboard
            }
            white_pawns = white_pawns xor targetBitboard
        }
    }

    private fun moveBlackPawn(fromBitboard: Long, targetBitboard: Long, move: Int, isApply: Boolean) {
        if (isApply) {
            if ((targetBitboard and first_row) != 0L) {
                val promotionPiece = UnapplyableMove.promotionPiece(move)
                pieces[numberOfTrailingZeros(fromBitboard)] = ColoredPieceType.NO_PIECE
                pieces[numberOfTrailingZeros(targetBitboard)] = promotionPiece
                promoteBlackPawn(targetBitboard, promotionPiece)
            } else {
                black_pawns = black_pawns xor targetBitboard
            }
            black_pawns = black_pawns xor fromBitboard
        } else {
            if ((fromBitboard and first_row) != 0L) {
                val promotionPiece = UnapplyableMove.promotionPiece(move)
                pieces[numberOfTrailingZeros(fromBitboard)] = ColoredPieceType.NO_PIECE
                pieces[numberOfTrailingZeros(targetBitboard)] = ColoredPieceType.BLACK_PAWN_BYTE
                promoteBlackPawn(fromBitboard, promotionPiece)
            } else {
                black_pawns = black_pawns xor fromBitboard
            }
            black_pawns = black_pawns xor targetBitboard
        }
    }

    // TODO: Do with array
    private fun promoteWhitePawn(bitboard: Long, promotionPiece: Byte) {
        when (promotionPiece) {
            ColoredPieceType.WHITE_KNIGHT_BYTE -> white_knights = white_knights xor bitboard
            ColoredPieceType.WHITE_BISHOP_BYTE -> white_bishops = white_bishops xor bitboard
            ColoredPieceType.WHITE_ROOK_BYTE -> white_rooks = white_rooks xor bitboard
            ColoredPieceType.WHITE_QUEEN_BYTE -> white_queens = white_queens xor bitboard
        }
    }

    // TODO: Do with array
    private fun promoteBlackPawn(bitboard: Long, promotionPiece: Byte) {
        when (promotionPiece) {
            ColoredPieceType.BLACK_KNIGHT_BYTE -> black_knights = black_knights xor bitboard
            ColoredPieceType.BLACK_BISHOP_BYTE -> black_bishops = black_bishops xor bitboard
            ColoredPieceType.BLACK_ROOK_BYTE -> black_rooks = black_rooks xor bitboard
            ColoredPieceType.BLACK_QUEEN_BYTE -> black_queens = black_queens xor bitboard
        }
    }

    private fun moveWhiteBishop(fromBitboard: Long, targetBitboard: Long) {
        white_bishops = white_bishops xor (fromBitboard xor targetBitboard)
    }

    private fun moveBlackBishop(fromBitboard: Long, targetBitboard: Long) {
        black_bishops = black_bishops xor (fromBitboard xor targetBitboard)
    }

    private fun moveBlackKnight(fromBitboard: Long, targetBitboard: Long) {
        black_knights = black_knights xor (fromBitboard xor targetBitboard)
    }

    private fun moveWhiteKnight(fromBitboard: Long, targetBitboard: Long) {
        white_knights = white_knights xor (fromBitboard xor targetBitboard)
    }

    private fun moveWhiteRook(fromBitboard: Long, targetBitboard: Long) {
        if (fromBitboard == a1Bitboard) {
            white_king_or_rook_queen_side_moved = true
        } else if (fromBitboard == h1Bitboard) {
            white_king_or_rook_king_side_moved = true
        }
        white_rooks = white_rooks xor (fromBitboard xor targetBitboard)
    }

    private fun moveBlackRook(fromBitboard: Long, targetBitboard: Long) {
        if (fromBitboard == a8Bitboard) {
            black_king_or_rook_queen_side_moved = true
        } else if (fromBitboard == h8Bitboard) {
            black_king_or_rook_king_side_moved = true
        }
        black_rooks = black_rooks xor (fromBitboard xor targetBitboard)
    }

    private fun moveBlackQueen(fromBitboard: Long, targetBitboard: Long) {
        black_queens = black_queens xor (fromBitboard xor targetBitboard)
    }

    private fun moveWhiteQueen(fromBitboard: Long, targetBitboard: Long) {
        white_queens = white_queens xor (fromBitboard xor targetBitboard)
    }

    private fun moveWhiteKing(fromBitboard: Long, targetBitboard: Long, isApply: Boolean) {
        white_kings = white_kings xor (fromBitboard xor targetBitboard)

        if (isApply) {
            if (fromBitboard == e1Bitboard) {
                white_king_or_rook_king_side_moved = true
                white_king_or_rook_queen_side_moved = true
                if (targetBitboard == c1Bitboard) {
                    white_rooks = white_rooks xor a1Bitboard xor d1Bitboard
                    pieces[d1FieldIdx.toInt()] = ColoredPieceType.WHITE_ROOK_BYTE
                    pieces[a1FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                } else if (targetBitboard == g1Bitboard) {
                    white_rooks = white_rooks xor h1Bitboard xor f1Bitboard
                    pieces[f1FieldIdx.toInt()] = ColoredPieceType.WHITE_ROOK_BYTE
                    pieces[h1FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                }
            }
        } else {
            if (targetBitboard == e1Bitboard) {
                if (fromBitboard == c1Bitboard) {
                    white_king_or_rook_queen_side_moved = true
                    white_rooks = white_rooks xor a1Bitboard xor d1Bitboard
                    pieces[a1FieldIdx.toInt()] = ColoredPieceType.WHITE_ROOK_BYTE
                    pieces[d1FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                } else if (fromBitboard == g1Bitboard) {
                    white_king_or_rook_king_side_moved = true
                    white_rooks = white_rooks xor h1Bitboard xor f1Bitboard
                    pieces[h1FieldIdx.toInt()] = ColoredPieceType.WHITE_ROOK_BYTE
                    pieces[f1FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                }
            }
        }
    }

    private fun moveBlackKing(fromBitboard: Long, targetBitboard: Long, isApply: Boolean) {
        black_kings = black_kings xor fromBitboard
        black_kings = black_kings xor targetBitboard

        // Handle castling
        if (isApply) {
            if (fromBitboard == e8Bitboard) {
                black_king_or_rook_king_side_moved = true
                black_king_or_rook_queen_side_moved = true
                if (targetBitboard == c8Bitboard) {
                    black_rooks = black_rooks xor a8Bitboard xor d8Bitboard
                    pieces[d8FieldIdx.toInt()] = ColoredPieceType.BLACK_ROOK_BYTE
                    pieces[a8FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                } else if (targetBitboard == g8Bitboard) {
                    black_rooks = black_rooks xor h8Bitboard xor f8Bitboard
                    pieces[f8FieldIdx.toInt()] = ColoredPieceType.BLACK_ROOK_BYTE
                    pieces[h8FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                }
            }
        } else {
            if (targetBitboard == e8Bitboard) {
                if (fromBitboard == c8Bitboard) {
                    black_king_or_rook_queen_side_moved = false
                    black_rooks = black_rooks xor a8Bitboard xor d8Bitboard
                    pieces[a8FieldIdx.toInt()] = ColoredPieceType.BLACK_ROOK_BYTE
                    pieces[d8FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                } else if (fromBitboard == g8Bitboard) {
                    black_king_or_rook_king_side_moved = false
                    black_rooks = black_rooks xor h8Bitboard xor f8Bitboard
                    pieces[h8FieldIdx.toInt()] = ColoredPieceType.BLACK_ROOK_BYTE
                    pieces[f8FieldIdx.toInt()] = ColoredPieceType.NO_PIECE
                }
            }
        }
    }

    fun finalizeBitboards() {
        occupiedSquares[ColorUtils.WHITE.toInt()] = white_pawns or white_knights or white_bishops or white_rooks or white_queens or white_kings
        occupiedSquares[ColorUtils.BLACK.toInt()] = black_pawns or black_knights or black_bishops or black_rooks or black_queens or black_kings

        occupied_board = occupiedSquares[ColorUtils.WHITE.toInt()] or occupiedSquares[ColorUtils.BLACK.toInt()]
        unoccupied_board = occupied_board.inv()
        enemy_and_empty_board = occupiedSquares[ColorUtils.opponent(toMove).toInt()] or unoccupied_board

        computeZobristHash()
    }

    fun finalizeBitboardsAfterApply(
        fromIdx: Int,
        targetIdx: Int,
        movingPiece: Byte,
        takenPiece: Byte,
        promotionPiece: Byte
    ) {
        mutateGeneralBoardOccupation()
        mutateZobristHash(fromIdx, targetIdx, movingPiece, takenPiece, promotionPiece)
    }

    fun mutateGeneralBoardOccupation() {
        // TODO: Test this hashing
        occupied_board = occupiedSquares[ColorUtils.WHITE.toInt()] or occupiedSquares[ColorUtils.BLACK.toInt()]
        unoccupied_board = occupied_board.inv()
        val opponent = ColorUtils.opponent(toMove)
        enemy_and_empty_board = occupiedSquares[opponent.toInt()] or unoccupied_board
    }

    private fun mutateZobristHash(
        fromIdx: Int,
        targetIdx: Int,
        movingPiece: Byte,
        takenPiece: Byte,
        promotionPiece: Byte
    ) {
        zobristHash = zobristHash xor
                bitsToSwap(fromIdx, movingPiece) xor
                bitsToSwap(targetIdx, if(promotionPiece != ColoredPieceType.NO_PIECE) promotionPiece else movingPiece) xor
                bitsToSwap(targetIdx, takenPiece)
    }

    // Only to be calculated once. After that, it should recalculate the hash using incremental updates
    private fun computeZobristHash() {
        zobristHash = 1659068882
        for (fieldIdx in 0..63) {
            val coloredPiece: Byte = coloredPiece(fieldIdx.toByte())
            if (coloredPiece != ColoredPieceType.NO_PIECE) {
                zobristHash = zobristHash xor bitsToSwap(fieldIdx, coloredPiece)
            }
        }
    }

    private fun bitsToSwap(fieldIdx: Int, coloredPiece: Byte): Int {
        return zobristPieceHash(fieldIdx, coloredPiece)
    }

    private fun zobristPieceHash(fieldIdx: Int, coloredPiece: Byte): Int {
        return zobristRandomTable[fieldIdx][coloredPiece.toInt()]
    }

    fun addPiece(color: Byte, pieceType: PieceType, fieldIndex: Byte) {
        if (PieceType.KING == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_kings = white_kings or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_kings = black_kings or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        if (PieceType.QUEEN == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_queens = white_queens or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_queens = black_queens or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        if (PieceType.ROOK == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_rooks = white_rooks or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_rooks = black_rooks or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        if (PieceType.BISHOP == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_bishops = white_bishops or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_bishops = black_bishops or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        if (PieceType.KNIGHT == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_knights = white_knights or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_knights = black_knights or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        if (PieceType.PAWN == pieceType) {
            if (color == ColorUtils.WHITE) {
                white_pawns = white_pawns or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            } else {
                black_pawns = black_pawns or BitboardUtils.bitboardFromFieldIdx(fieldIndex)
            }
        }

        pieces[fieldIndex.toInt()] = ColoredPieceType.getColoredByte(pieceType, color)
    }

    fun cloneBoard(toMove: Byte, castlingEnabled: Boolean): ACEBoard {
        val board = cloneBoard()
        board.toMove = toMove
        if (!castlingEnabled) {
            board.white_king_or_rook_queen_side_moved = true
            board.white_king_or_rook_king_side_moved = true
            board.black_king_or_rook_queen_side_moved = true
            board.black_king_or_rook_king_side_moved = true
        }
        board.finalizeBitboards()
        return board
    }

    fun cloneBoard(): ACEBoard {
        val clonedBoard = ACEBoard()
        clonedBoard.black_kings = black_kings
        clonedBoard.white_kings = white_kings
        clonedBoard.black_queens = black_queens
        clonedBoard.white_queens = white_queens
        clonedBoard.white_rooks = white_rooks
        clonedBoard.black_rooks = black_rooks
        clonedBoard.white_bishops = white_bishops
        clonedBoard.black_bishops = black_bishops
        clonedBoard.white_knights = white_knights
        clonedBoard.black_knights = black_knights
        clonedBoard.white_pawns = white_pawns
        clonedBoard.black_pawns = black_pawns

        clonedBoard.white_king_or_rook_queen_side_moved = white_king_or_rook_queen_side_moved
        clonedBoard.white_king_or_rook_king_side_moved = white_king_or_rook_king_side_moved
        clonedBoard.black_king_or_rook_queen_side_moved = black_king_or_rook_queen_side_moved
        clonedBoard.black_king_or_rook_king_side_moved = black_king_or_rook_king_side_moved

        clonedBoard.toMove = toMove
        clonedBoard.zobristHash = 0
        clonedBoard.pieces = pieces.copyOf(pieces.size)
        clonedBoard.finalizeBitboards()
        return clonedBoard
    }

    @Throws(KingEatingException::class)
    fun generateMoves(): MutableList<Int> {
        return AceMoveGenerator.generateMoves(this)
    }

    @Throws(KingEatingException::class)
    fun generateTakeMoves(): List<Int> {
        return AceTakeMoveGenerator.generateTakeMoves(this)
    }

    fun canTakeKing(): Boolean {
        try {
            AceTakeMoveGenerator.generateTakeMoves(this)
            return false
        } catch (e: KingEatingException) {
            return true
        }
    }

    fun getToMove(): Int {
        return toMove.toInt()
    }

    fun hasNoKing(): Boolean {
        return if (ColorUtils.isWhite(toMove.toInt())) {
            white_kings == 0L
        } else {
            black_kings == 0L
        }
    }

    fun breakpoint(): Boolean {
//        val match = stringDump(this).contains(
//            "pieces=\n" +
//                    "......♚.\n" +
//                    ".....♟♟♟\n" +
//                    "....♟...\n" +
//                    ".♟......\n" +
//                    "♙.......\n" +
//                    ".♙....♙.\n" +
//                    "♔.......\n" +
//                    "........"
//        )
        val breakpointHit = ENGINE_DEBUG && (
                stringDump(this).hashCode() == -97317686 ||
                        piecesToString(pieces) == "" +
                        "......♚.\n" +
                        ".....♟♟♟\n" +
                        "....♟...\n" +
                        ".♟......\n" +
                        "♙.......\n" +
                        ".♙....♙.\n" +
                        "♔.......\n" +
                        "........" ||
                        pieces[FieldUtils.fieldIdx("g3").toInt()] == ColoredPieceType.BLACK_PAWN_BYTE
                )
        return breakpointHit
    }

    companion object {
        // Used for testing and creating an initial position
        @JvmStatic
        fun emptyBoard(toMove: Byte, castlingEnabled: Boolean): ACEBoard {
            val board = ACEBoard()
            board.toMove = toMove
            if (!castlingEnabled) {
                board.white_king_or_rook_queen_side_moved = true
                board.white_king_or_rook_king_side_moved = true
                board.black_king_or_rook_queen_side_moved = true
                board.black_king_or_rook_king_side_moved = true
            }
            return board
        }
    }
}
