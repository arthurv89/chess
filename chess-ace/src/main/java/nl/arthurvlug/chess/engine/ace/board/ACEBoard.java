package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceTakeMoveGenerator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.board.pieces.PieceTypeBytes;

import static nl.arthurvlug.chess.engine.ColorUtils.*;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldIdx;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;
import static nl.arthurvlug.chess.utils.MoveUtils.DEBUG;

public class ACEBoard {
	public byte toMove;

//	public long[][] pieces = new long[PieceType.values().length][2];
	public long black_kings;
	public long white_kings;
	public long black_queens;
	public long white_queens;
	public long white_rooks;
	public long black_rooks;
	public long white_bishops;
	public long black_bishops;
	public long white_knights;
	public long black_knights;
	public long white_pawns;
	public long black_pawns;

	public long[] occupiedSquares = new long[2];
	public long unoccupied_board;
	public long occupied_board;
	public long enemy_and_empty_board;

	// For castling
	public boolean white_king_or_rook_queen_side_moved;
	public boolean white_king_or_rook_king_side_moved;
	public boolean black_king_or_rook_queen_side_moved;
	public boolean black_king_or_rook_king_side_moved;

	private short[] pieces = new short[64];

	// TODO: Implement
	private int fiftyMove = 0;

	// TODO: Implement
	private int repeatedMove = 0;

	// Black rook positions
	private static final long a8Bitboard = bitboardFromFieldName("a8");
	private static final long d8Bitboard = bitboardFromFieldName("d8");
	private static final long f8Bitboard = bitboardFromFieldName("f8");
	private static final long h8Bitboard = bitboardFromFieldName("h8");
	// King positions
	private static final long g8Bitboard = bitboardFromFieldName("g8");
	private static final long c8Bitboard = bitboardFromFieldName("c8");
	public static final long e8Bitboard = bitboardFromFieldName("e8");

	// White castling positions
	private static final long a1Bitboard = bitboardFromFieldName("a1");
	private static final long d1Bitboard = bitboardFromFieldName("d1");
	private static final long f1Bitboard = bitboardFromFieldName("f1");
	private static final long h1Bitboard = bitboardFromFieldName("h1");
	// King positions
	private static final long g1Bitboard = bitboardFromFieldName("g1");
	private static final long c1Bitboard = bitboardFromFieldName("c1");
	public static final long e1Bitboard = bitboardFromFieldName("e1");

	public final static long first_row = bitboardFromFieldName("a1 b1 c1 d1 e1 f1 g1 h1");
	public final static long last_row = bitboardFromFieldName("a8 b8 c8 d8 e8 f8 g8 h8");

	private final static short d1FieldIdx = FieldUtils.fieldIdx("d1");
	private final static short f1FieldIdx = FieldUtils.fieldIdx("f1");
	private final static short d8FieldIdx = FieldUtils.fieldIdx("d8");
	private final static short f8FieldIdx = FieldUtils.fieldIdx("f8");

	private int zobristHash;
	private static int[][] zobristRandomTable = new int[64][13];

	public Stack<Integer> plyStack = new Stack<>();
	private boolean incFiftyClock;


	protected ACEBoard() {
		final Random random = new Random(1);
		for (int fieldIndex = 0; fieldIndex<64; fieldIndex++) {
			for (final PieceType pieceType : PieceType.values()) {
				for (byte color = 0; color<2; color++) {
					byte coloredByte = ColoredPieceType.getColoredByte(pieceType, color);
					int rand = random.nextInt();
					zobristRandomTable[fieldIndex][coloredByte] = rand;
				}
			}
		}
	}

	public void apply(List<String> moveList) {
		if(moveList.isEmpty()) {
			return;
		}

		for(String sMove : moveList) {
			apply(sMove);
		}
	}

	public void apply(final String sMove) {
		final int move = UnapplyableMoveUtils.createMove(sMove, this);
		final byte movingPiece = UnapplyableMove.coloredMovingPiece(move);
		if(DEBUG && movingPiece == NO_PIECE) {
			throw new RuntimeException("Could not determine moving piece while executing " + UnapplyableMoveUtils.toString(move));
		}
		apply(move);
	}

	public short coloredPiece(String fieldName) {
		return coloredPiece(FieldUtils.fieldIdx(fieldName));
	}

	public byte coloredPiece(byte fieldIdx) {
		long bitboard = bitboardFromFieldIdx(fieldIdx);

		if((white_pawns & bitboard) != 0L)   return WHITE_PAWN_BYTE;
		if((white_knights & bitboard) != 0L) return WHITE_KNIGHT_BYTE;
		if((white_bishops & bitboard) != 0L) return WHITE_BISHOP_BYTE;
		if((white_rooks & bitboard) != 0L)   return WHITE_ROOK_BYTE;
		if((white_queens & bitboard) != 0L)  return WHITE_QUEEN_BYTE;
		if((white_kings & bitboard) != 0L)   return WHITE_KING_BYTE;

		if((black_pawns & bitboard) != 0L)   return BLACK_PAWN_BYTE;
		if((black_knights & bitboard) != 0L) return BLACK_KNIGHT_BYTE;
		if((black_bishops & bitboard) != 0L) return BLACK_BISHOP_BYTE;
		if((black_rooks & bitboard) != 0L)   return BLACK_ROOK_BYTE;
		if((black_queens & bitboard) != 0L)  return BLACK_QUEEN_BYTE;
		if((black_kings & bitboard) != 0L)   return BLACK_KING_BYTE;

		return NO_PIECE;
	}

	public int pieceType(final byte fieldIdx) {
		long bitboard = bitboardFromFieldIdx(fieldIdx);

		if((white_pawns & bitboard) != 0L)   return PieceTypeBytes.PAWN_BYTE;
		if((white_knights & bitboard) != 0L) return PieceTypeBytes.KNIGHT_BYTE;
		if((white_bishops & bitboard) != 0L) return PieceTypeBytes.BISHOP_BYTE;
		if((white_rooks & bitboard) != 0L)   return PieceTypeBytes.ROOK_BYTE;
		if((white_queens & bitboard) != 0L)  return PieceTypeBytes.QUEEN_BYTE;
		if((white_kings & bitboard) != 0L)   return PieceTypeBytes.KING_BYTE;

		if((black_pawns & bitboard) != 0L)   return PieceTypeBytes.PAWN_BYTE;
		if((black_knights & bitboard) != 0L) return PieceTypeBytes.KNIGHT_BYTE;
		if((black_bishops & bitboard) != 0L) return PieceTypeBytes.BISHOP_BYTE;
		if((black_rooks & bitboard) != 0L)   return PieceTypeBytes.ROOK_BYTE;
		if((black_queens & bitboard) != 0L)  return PieceTypeBytes.QUEEN_BYTE;
		if((black_kings & bitboard) != 0L)   return PieceTypeBytes.KING_BYTE;

		return NO_PIECE;
	}

	public void apply(int move) {
		incFiftyClock = true;
		plyStack.push(move);
		// TODO: Fix this by creating a more efficient Move object
		byte fromIdx = UnapplyableMove.fromIdx(move);
		long fromBitboard = bitboardFromFieldIdx(fromIdx);

		byte targetIdx = UnapplyableMove.targetIdx(move);
		long targetBitboard = bitboardFromFieldIdx(targetIdx);

		byte coloredMovingPiece = UnapplyableMove.coloredMovingPiece(move);

		xorMove(targetBitboard, fromBitboard, coloredMovingPiece, move, true);
		xorTakePiece(move, targetBitboard, targetIdx);

		pieces[fromIdx] = NO_PIECE;
		pieces[targetIdx] = coloredMovingPiece;

		if(incFiftyClock) {
			fiftyMove++;
		} else {
			fiftyMove = 0;
		}
		toMove = opponent(toMove);

		finalizeBitboardsAfterApply(fromIdx, targetIdx, coloredMovingPiece, UnapplyableMove.takePiece(move));

		if(DEBUG) {
			Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
			Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
			Preconditions.checkArgument((occupiedSquares[WHITE] & occupiedSquares[BLACK]) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.toString((occupiedSquares[WHITE] & occupiedSquares[BLACK])));
		}
	}

	public void unapply(final int move,
						final boolean white_king_or_rook_queen_side_moved_before,
						final boolean white_king_or_rook_king_side_moved_before,
						final boolean black_king_or_rook_queen_side_moved_before,
						final boolean black_king_or_rook_king_side_moved_before,
						final int fiftyMove_before) {
		plyStack.pop();
		toMove = opponent(toMove);

		byte targetIdx = UnapplyableMove.targetIdx(move);
		long targetBitboard = bitboardFromFieldIdx(targetIdx);
		final byte coloredMovingPiece = UnapplyableMove.coloredMovingPiece(move);

		byte fromIdx = UnapplyableMove.fromIdx(move);
		long fromBitboard = bitboardFromFieldIdx(fromIdx);

		// This is a reverse move
		pieces[targetIdx] = NO_PIECE;
		pieces[fromIdx] = coloredMovingPiece;

		xorMove(fromBitboard, targetBitboard, coloredMovingPiece, move, false);
		xorTakePiece(move, targetBitboard, targetIdx);

		this.white_king_or_rook_queen_side_moved = white_king_or_rook_queen_side_moved_before;
		this.white_king_or_rook_king_side_moved = white_king_or_rook_king_side_moved_before;
		this.black_king_or_rook_queen_side_moved = black_king_or_rook_queen_side_moved_before;
		this.black_king_or_rook_king_side_moved = black_king_or_rook_king_side_moved_before;

		finalizeBitboardsAfterApply(fromIdx, targetIdx, coloredMovingPiece, UnapplyableMove.takePiece(move));
		this.fiftyMove = fiftyMove_before;

		if(DEBUG) {
			Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
			Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
			Preconditions.checkArgument((occupiedSquares[WHITE] & occupiedSquares[BLACK]) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.toString((occupiedSquares[WHITE] & occupiedSquares[BLACK])));
		}
	}

	private void xorMove(final long targetBitboard, final long fromBitboard,
						 final short coloredMovingPiece,
						 final int move,
						 final boolean isApply) {
		// TODO: Remove if statement
		if(isWhite(toMove)) {
			switch (coloredMovingPiece) {
				case WHITE_PAWN_BYTE:   incFiftyClock = false; moveWhitePawn(fromBitboard, targetBitboard, move, isApply); break;
				case WHITE_KNIGHT_BYTE: moveWhiteKnight(fromBitboard, targetBitboard); break;
				case WHITE_BISHOP_BYTE: moveWhiteBishop(fromBitboard, targetBitboard); break;
				case WHITE_ROOK_BYTE:   moveWhiteRook  (fromBitboard, targetBitboard); break;
				case WHITE_QUEEN_BYTE:  moveWhiteQueen (fromBitboard, targetBitboard); break;
				case WHITE_KING_BYTE:   moveWhiteKing  (fromBitboard, targetBitboard, isApply); break;
			}
			recalculateWhiteOccupiedSquares();
		} else {
			switch (coloredMovingPiece) {
				case BLACK_PAWN_BYTE:   incFiftyClock = false; moveBlackPawn(fromBitboard, targetBitboard, move, isApply); break;
				case BLACK_KNIGHT_BYTE: moveBlackKnight(fromBitboard, targetBitboard); break;
				case BLACK_BISHOP_BYTE: moveBlackBishop(fromBitboard, targetBitboard); break;
				case BLACK_ROOK_BYTE:   moveBlackRook  (fromBitboard, targetBitboard); break;
				case BLACK_QUEEN_BYTE:  moveBlackQueen (fromBitboard, targetBitboard); break;
				case BLACK_KING_BYTE:   moveBlackKing  (fromBitboard, targetBitboard, isApply); break;
			}
			recalculateBlackOccupiedSquares();
		}
	}

	private void recalculateWhiteOccupiedSquares() {
		occupiedSquares[WHITE] = white_pawns | white_knights | white_bishops | white_rooks | white_queens | white_kings;
	}

	private void recalculateBlackOccupiedSquares() {
		occupiedSquares[BLACK] = black_pawns | black_knights | black_bishops | black_rooks | black_queens | black_kings;
	}

	private void xorTakePiece(final int move, final long targetBitboard, final int targetIdx) {
		short takePiece = UnapplyableMove.takePiece(move);
		if(isWhite(toMove)) {
			switch (takePiece) {
				case NO_PIECE: return;
				case BLACK_PAWN_BYTE:
					pieces[targetIdx] = BLACK_PAWN_BYTE;
					incFiftyClock=false; black_pawns   ^= targetBitboard; break;
				case BLACK_KNIGHT_BYTE:
					pieces[targetIdx] = BLACK_KNIGHT_BYTE;
					incFiftyClock=false; black_knights ^= targetBitboard; break;
				case BLACK_BISHOP_BYTE:
					pieces[targetIdx] = BLACK_BISHOP_BYTE;
					incFiftyClock=false; black_bishops ^= targetBitboard; break;
				case BLACK_ROOK_BYTE:
					pieces[targetIdx] = BLACK_ROOK_BYTE;
					incFiftyClock=false; takeBlackRook(targetBitboard); break;
				case BLACK_QUEEN_BYTE:
					pieces[targetIdx] = BLACK_QUEEN_BYTE;
					incFiftyClock=false; black_queens  ^= targetBitboard; break;
				case BLACK_KING_BYTE:
					pieces[targetIdx] = BLACK_KING_BYTE;
					incFiftyClock=false; black_kings   ^= targetBitboard; break;
			}
			// TODO: Move this so we don't have to do the if statement
			recalculateBlackOccupiedSquares();
		} else {
			switch (takePiece) {
				case NO_PIECE:
					return;
				case WHITE_PAWN_BYTE:
					pieces[targetIdx] = WHITE_PAWN_BYTE;
					incFiftyClock=false; white_pawns   ^= targetBitboard; break;
				case WHITE_KNIGHT_BYTE:
					pieces[targetIdx] = WHITE_KNIGHT_BYTE;
					incFiftyClock=false; white_knights ^= targetBitboard; break;
				case WHITE_BISHOP_BYTE:
					pieces[targetIdx] = WHITE_BISHOP_BYTE;
					incFiftyClock=false; white_bishops ^= targetBitboard; break;
				case WHITE_ROOK_BYTE:
					pieces[targetIdx] = WHITE_ROOK_BYTE;
					incFiftyClock=false; takeWhiteRook(targetBitboard); break;
				case WHITE_QUEEN_BYTE:
					pieces[targetIdx] = WHITE_QUEEN_BYTE;
					incFiftyClock=false; white_queens  ^= targetBitboard; break;
				case WHITE_KING_BYTE:
					pieces[targetIdx] = WHITE_PAWN_BYTE;
					incFiftyClock=false; white_kings   ^= targetBitboard; break;
			}
			recalculateWhiteOccupiedSquares();
		}
	}

	private void takeWhiteRook(final long targetBitboard) {
		if(targetBitboard == a1Bitboard) {
			white_king_or_rook_queen_side_moved = true;
		} else if(targetBitboard == h1Bitboard) {
			white_king_or_rook_king_side_moved = true;
		}
		white_rooks   ^= targetBitboard;

	}

	private void takeBlackRook(final long targetBitboard) {
		if(targetBitboard == a8Bitboard) {
			black_king_or_rook_queen_side_moved = true;
		} else if(targetBitboard == h8Bitboard) {
			black_king_or_rook_king_side_moved = true;
		}
		black_rooks   ^= targetBitboard;
	}

	private void moveWhitePawn(final long fromBitboard, final long targetBitboard, final int move, boolean isApply) {
		if(isApply) {
			if ((targetBitboard & last_row) != 0L) {
				final byte promotionPiece = UnapplyableMove.promotionPiece(move);
				pieces[Long.numberOfTrailingZeros(fromBitboard)] = NO_PIECE;
				promoteWhitePawn(targetBitboard, promotionPiece);
			} else {
				white_pawns ^= targetBitboard;
			}
			white_pawns ^= fromBitboard;
		} else {
			if ((fromBitboard & last_row) != 0L) {
				final byte promotionPiece = UnapplyableMove.promotionPiece(move);
				promoteWhitePawn(fromBitboard, promotionPiece);
			} else {
				white_pawns ^= fromBitboard;
			}
			white_pawns ^= targetBitboard;
		}
	}

	private void moveBlackPawn(final long fromBitboard, final long targetBitboard, final int move, final boolean isApply) {
		if(isApply) {
			if((targetBitboard & first_row) != 0L) {
				final byte promotionPiece = UnapplyableMove.promotionPiece(move);
				promoteBlackPawn(targetBitboard, promotionPiece);
			} else {
				black_pawns ^= targetBitboard;
			}
			black_pawns ^= fromBitboard;
		} else {
			if ((fromBitboard & first_row) != 0L) {
				final byte promotionPiece = UnapplyableMove.promotionPiece(move);
				pieces[Long.numberOfTrailingZeros(fromBitboard)] = NO_PIECE;
				promoteBlackPawn(fromBitboard, promotionPiece);
			} else {
				black_pawns ^= fromBitboard;
			}
			black_pawns ^= targetBitboard;
		}
	}

	// TODO: Do with array
	private void promoteWhitePawn(final long bitboard, final byte promotionPiece) {
		switch (promotionPiece) {
			case WHITE_KNIGHT_BYTE: white_knights ^= bitboard; break;
			case WHITE_BISHOP_BYTE: white_bishops ^= bitboard; break;
			case WHITE_ROOK_BYTE: white_rooks ^= bitboard; break;
			case WHITE_QUEEN_BYTE: white_queens ^= bitboard; break;
		}
	}

	// TODO: Do with array
	private void promoteBlackPawn(final long bitboard, final byte promotionPiece) {
		switch (promotionPiece) {
			case BLACK_KNIGHT_BYTE: black_knights ^= bitboard; break;
			case BLACK_BISHOP_BYTE: black_bishops ^= bitboard; break;
			case BLACK_ROOK_BYTE: black_rooks ^= bitboard; break;
			case BLACK_QUEEN_BYTE: black_queens ^= bitboard; break;
		}
	}

	private void moveWhiteBishop(final long fromBitboard, final long targetBitboard) {
		white_bishops ^= fromBitboard;
		white_bishops ^= targetBitboard;
	}

	private void moveBlackBishop(final long fromBitboard, final long targetBitboard) {
		black_bishops ^= fromBitboard;
		black_bishops ^= targetBitboard;
	}

	private void moveBlackKnight(final long fromBitboard, final long targetBitboard) {
		black_knights ^= fromBitboard;
		black_knights ^= targetBitboard;
	}

	private void moveWhiteKnight(final long fromBitboard, final long targetBitboard) {
		white_knights ^= fromBitboard;
		white_knights ^= targetBitboard;
	}

	private void moveWhiteRook(final long fromBitboard, final long targetBitboard) {
		if(fromBitboard == a1Bitboard) {
			white_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h1Bitboard) {
			white_king_or_rook_king_side_moved = true;
		}
		white_rooks ^= fromBitboard;
		white_rooks ^= targetBitboard;
	}

	private void moveBlackRook(final long fromBitboard, final long targetBitboard) {
		if(fromBitboard == a8Bitboard) {
			black_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h8Bitboard) {
			black_king_or_rook_king_side_moved = true;
		}
		black_rooks ^= fromBitboard;
		black_rooks ^= targetBitboard;
	}

	private void moveBlackQueen(final long fromBitboard, final long targetBitboard) {
		black_queens ^= fromBitboard;
		black_queens ^= targetBitboard;
	}

	private void moveWhiteQueen(final long fromBitboard, final long targetBitboard) {
		white_queens ^= fromBitboard;
		white_queens ^= targetBitboard;
	}

	private void moveWhiteKing(final long fromBitboard, final long targetBitboard, final boolean isApply) {
		white_kings ^= fromBitboard;
		white_kings ^= targetBitboard;
		if(isApply) {
			if (fromBitboard == e1Bitboard) {
				if (targetBitboard == c1Bitboard) {
					white_king_or_rook_queen_side_moved = true;
					white_rooks ^= a1Bitboard;
					white_rooks ^= d1Bitboard;
					pieces[d1FieldIdx] = WHITE_ROOK_BYTE;
				} else if (targetBitboard == g1Bitboard) {
					white_king_or_rook_king_side_moved = true;
					white_rooks ^= h1Bitboard;
					white_rooks ^= f1Bitboard;
					pieces[f1FieldIdx] = WHITE_ROOK_BYTE;
				}
			}
		} else {
			if (targetBitboard == e1Bitboard) {
				if (fromBitboard == c1Bitboard) {
					white_king_or_rook_queen_side_moved = false;
					white_rooks ^= a1Bitboard;
					white_rooks ^= d1Bitboard;
					pieces[d1FieldIdx] = NO_PIECE;
				} else if (fromBitboard == g1Bitboard) {
					white_king_or_rook_king_side_moved = false;
					white_rooks ^= h1Bitboard;
					white_rooks ^= f1Bitboard;
					pieces[d8FieldIdx] = NO_PIECE;
				}
			}
		}
	}

	private void moveBlackKing(final long fromBitboard, final long targetBitboard, final boolean isApply) {
		black_kings ^= fromBitboard;
		black_kings ^= targetBitboard;

		// Handle castling
		if(isApply) {
			if (fromBitboard == e8Bitboard) {
				if (targetBitboard == c8Bitboard) {
					black_king_or_rook_queen_side_moved = true;
					black_rooks ^= a8Bitboard;
					this.black_rooks ^= d8Bitboard;
					pieces[d8FieldIdx] = BLACK_ROOK_BYTE;
				} else if (targetBitboard == g8Bitboard) {
					black_king_or_rook_king_side_moved = true;
					black_rooks ^= h8Bitboard;
					black_rooks ^= f8Bitboard;
					pieces[f8FieldIdx] = BLACK_ROOK_BYTE;
				}
			}
		} else {
			if (targetBitboard == e8Bitboard) {
				if (fromBitboard == c8Bitboard) {
					black_king_or_rook_queen_side_moved = false;
					black_rooks ^= a8Bitboard;
					black_rooks ^= d8Bitboard;
					pieces[d8FieldIdx] = NO_PIECE;
				} else if (fromBitboard == g8Bitboard) {
					black_king_or_rook_king_side_moved = false;
					black_rooks ^= h8Bitboard;
					black_rooks ^= f8Bitboard;
					pieces[f8FieldIdx] = NO_PIECE;
				}
			}
		}
	}

	public void finalizeBitboards() {
		occupiedSquares[WHITE] = white_pawns | white_knights | white_bishops | white_rooks | white_queens | white_kings;
		occupiedSquares[BLACK] = black_pawns | black_knights | black_bishops | black_rooks | black_queens | black_kings;

		occupied_board = occupiedSquares[WHITE] | occupiedSquares[BLACK];
		unoccupied_board = ~occupied_board;
		enemy_and_empty_board = occupiedSquares[opponent(toMove)] | unoccupied_board;

		computeZobristHash();
	}

	private void finalizeBitboardsAfterApply(final int fromIdx, final int targetIdx,
											 final byte movingPiece, final byte takenPiece) {
		occupied_board = occupiedSquares[WHITE] | occupiedSquares[BLACK];
		unoccupied_board = ~occupied_board;
		byte opponent = opponent(toMove);
		enemy_and_empty_board = occupiedSquares[opponent] | unoccupied_board;

		// TODO: Test this hashing
		zobristHash ^= bitsToSwap(fromIdx, movingPiece);
		zobristHash ^= bitsToSwap(targetIdx, movingPiece);
		if(takenPiece != NO_PIECE) {
			zobristHash ^= bitsToSwap(targetIdx, takenPiece);
		}
	}

	// Only to be calculated once. After that, it should recalculate the hash using incremental updates
	private void computeZobristHash() {
		zobristHash = 1659068882;
		for (byte fieldIdx = 0; fieldIdx<64; fieldIdx++) {
			short coloredPiece = coloredPiece(fieldIdx);
			if(coloredPiece != NO_PIECE) {
				zobristHash ^= bitsToSwap(fieldIdx, coloredPiece);
			}
		}
	}

	private long bitsToSwap(final int fieldIdx, final short coloredPiece) {
		return zobristPieceHash(fieldIdx, coloredPiece);
	}

	private int zobristPieceHash(final int fieldIdx, final short coloredPiece) {
		return zobristRandomTable[fieldIdx][coloredPiece];
	}

	public String string() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		Preconditions.checkArgument((occupiedSquares[WHITE] & occupiedSquares[BLACK]) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.toString((occupiedSquares[WHITE] & occupiedSquares[BLACK])));
		
		StringBuilder sb = new StringBuilder();
		for (byte fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			if((fieldIdx)%8 == 0) {
				sb.append('\n');
			}

			byte coloredPieceOnField = coloredPiece(fieldIdx);
			if(coloredPieceOnField == NO_PIECE) {
				sb.append('.');
			} else {
				String c = PieceStringUtils.toCharacterString(ColoredPieceType.from(coloredPieceOnField), PieceStringUtils.pieceToChessSymbolMap);
				sb.append(c);
			}
		}
		String reversedBoard = sb.toString();
		
		// Reverse rows
		List<String> l = Lists.newArrayList(Splitter.on('\n').split(reversedBoard));
		Collections.reverse(l);
		return Joiner.on('\n').join(l);
	}

	public void addPiece(byte color, PieceType pieceType, byte fieldIndex) {
		if(PieceType.KING == pieceType) {
			if(color == WHITE) {
				white_kings |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_kings |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		if(PieceType.QUEEN == pieceType) {
			if(color == WHITE) {
				white_queens |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_queens |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		if(PieceType.ROOK == pieceType) {
			if(color == WHITE) {
				white_rooks |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_rooks |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		if(PieceType.BISHOP == pieceType) {
			if(color == WHITE) {
				white_bishops |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_bishops |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		if(PieceType.KNIGHT == pieceType) {
			if(color == WHITE) {
				white_knights |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_knights |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		if(PieceType.PAWN == pieceType) {
			if(color == WHITE) {
				white_pawns |= bitboardFromFieldIdx(fieldIndex);
			} else {
				black_pawns |= bitboardFromFieldIdx(fieldIndex);
			}
		}

		pieces[fieldIndex] = ColoredPieceType.getColoredByte(pieceType, color);
	}

	// Used for testing and creating an initial position
	public static ACEBoard emptyBoard(byte toMove, final boolean castlingEnabled) {
		final ACEBoard board = new ACEBoard();
		board.toMove = toMove;
		if(!castlingEnabled) {
			board.white_king_or_rook_queen_side_moved = true;
			board.white_king_or_rook_king_side_moved = true;
			board.black_king_or_rook_queen_side_moved = true;
			board.black_king_or_rook_king_side_moved = true;
		}
		return board;
	}

	public ACEBoard cloneBoard(final byte toMove, final boolean castlingEnabled) {
		final ACEBoard board = cloneBoard();
		board.toMove = toMove;
		if(!castlingEnabled) {
			board.white_king_or_rook_queen_side_moved = true;
			board.white_king_or_rook_king_side_moved = true;
			board.black_king_or_rook_queen_side_moved = true;
			board.black_king_or_rook_king_side_moved = true;
		}
		board.finalizeBitboards();
		return board;
	}

	public ACEBoard cloneBoard() {
		final ACEBoard clonedBoard = new ACEBoard();
		clonedBoard.black_kings = black_kings;
		clonedBoard.white_kings = white_kings;
		clonedBoard.black_queens = black_queens;
		clonedBoard.white_queens = white_queens;
		clonedBoard.white_rooks = white_rooks;
		clonedBoard.black_rooks = black_rooks;
		clonedBoard.white_bishops = white_bishops;
		clonedBoard.black_bishops = black_bishops;
		clonedBoard.white_knights = white_knights;
		clonedBoard.black_knights = black_knights;
		clonedBoard.white_pawns = white_pawns;
		clonedBoard.black_pawns = black_pawns;

		clonedBoard.white_king_or_rook_queen_side_moved = white_king_or_rook_queen_side_moved;
		clonedBoard.white_king_or_rook_king_side_moved = white_king_or_rook_king_side_moved;
		clonedBoard.black_king_or_rook_queen_side_moved = black_king_or_rook_queen_side_moved;
		clonedBoard.black_king_or_rook_king_side_moved = black_king_or_rook_king_side_moved;

		clonedBoard.toMove = toMove;
		clonedBoard.zobristHash = 0;
		clonedBoard.pieces = Arrays.copyOf(pieces, pieces.length);
		clonedBoard.finalizeBitboards();
		return clonedBoard;
	}

	public List<Integer> generateMoves() throws KingEatingException {
		return AceMoveGenerator.generateMoves(this);
	}

	public List<Integer> generateTakeMoves() throws KingEatingException {
		return AceTakeMoveGenerator.generateTakeMoves(this);
	}

	public int getRepeatedMove() {
		return repeatedMove;
	}

	public int getFiftyMoveClock() {
		return fiftyMove;
	}

	public int getToMove() {
		return toMove;
	}

	public boolean hasNoKing() {
		if(isWhite(toMove)) {
			return white_kings == 0;
		} else {
			return black_kings == 0;
		}
	}

	public int getZobristHash() {
		return zobristHash;
	}

	public short[] getPieces() {
		return pieces;
	}
}
