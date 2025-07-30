package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
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
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.a1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.a1FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.a8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.a8FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.c1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.c8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.d1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.d1FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.d8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.d8FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.e1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.e8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.f1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.f1FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.f8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.f8FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.first_row;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.g1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.g8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.h1Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.h1FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.h8Bitboard;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.h8FieldIdx;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.last_row;
import static nl.arthurvlug.chess.engine.ace.board.StaticAceBoard.zobristRandomTable;
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

	private byte[] pieces = new byte[64];

//	// TODO: Implement
//	private int fiftyMove = 0;

	// TODO: Implement
	private int repeatedMove = 0;

	private int zobristHash;

	public Stack<Integer> plyStack = new Stack<>();
//	public boolean incFiftyClock;


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
		System.out.println("XX");
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
//		hashCode() == 812553708 && move == 95954
//		incFiftyClock = true;
		plyStack.push(move);
		// TODO: Fix this by creating a more efficient Move object
		byte fromIdx = UnapplyableMove.fromIdx(move);
		long fromBitboard = bitboardFromFieldIdx(fromIdx);

		byte targetIdx = UnapplyableMove.targetIdx(move);
		long targetBitboard = bitboardFromFieldIdx(targetIdx);

		byte coloredMovingPiece = UnapplyableMove.coloredMovingPiece(move);

		xorMove(targetBitboard, fromBitboard, coloredMovingPiece, move, true);
		xorTakePiece(move, targetBitboard, targetIdx);

		int x = 1;
		breakpoint();

		pieces[fromIdx] = NO_PIECE;
		pieces[targetIdx] = coloredMovingPiece;

//		if(incFiftyClock) {
//			fiftyMove++;
//		} else {
//			fiftyMove = 0;
//		}
		toMove = opponent(toMove);
		breakpoint();

		finalizeBitboardsAfterApply(fromIdx, targetIdx, coloredMovingPiece, UnapplyableMove.takePiece(move));

		if(DEBUG) {
			checkConsistency();
		}
	}

	public void checkConsistency() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		long intersectBoard = occupiedSquares[WHITE] & occupiedSquares[BLACK];
		if(intersectBoard != 0) {
			throw new IllegalArgumentException(("""
                    White and black occupy the same fields.
                    
                    Offending field:
                    %s
                    White:
                    %s
                    Black:
                    %s
                    """)
					.formatted(BitboardUtils.toString(intersectBoard), whiteBoard(), blackBoard()));
		}
    }

	private String whiteBoard() {
		List<Byte> pieces = List.of(WHITE_PAWN_BYTE, WHITE_KNIGHT_BYTE, WHITE_BISHOP_BYTE, WHITE_ROOK_BYTE, WHITE_QUEEN_BYTE, WHITE_KING_BYTE);
		return string(pieces::contains);
	}

	private String blackBoard() {
		List<Byte> pieces = List.of(BLACK_PAWN_BYTE, BLACK_KNIGHT_BYTE, BLACK_BISHOP_BYTE, BLACK_ROOK_BYTE, BLACK_QUEEN_BYTE, BLACK_KING_BYTE);
		return string(pieces::contains);
	}

	public void unapply(final int move,
						final boolean white_king_or_rook_queen_side_moved_before,
						final boolean white_king_or_rook_king_side_moved_before,
						final boolean black_king_or_rook_queen_side_moved_before,
						final boolean black_king_or_rook_king_side_moved_before) {
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
//		this.fiftyMove = fiftyMove_before;

		if(DEBUG) {
			checkConsistency();
		}
	}

	private void xorMove(final long targetBitboard,
						 final long fromBitboard,
						 final short coloredMovingPiece,
						 final int move,
						 final boolean isApply) {
		// TODO: Remove if statement
		if(isWhite(toMove)) {
			switch (coloredMovingPiece) {
				case WHITE_PAWN_BYTE:   moveWhitePawn(fromBitboard, targetBitboard, move, isApply); break;
				case WHITE_KNIGHT_BYTE: moveWhiteKnight(fromBitboard, targetBitboard); break;
				case WHITE_BISHOP_BYTE: moveWhiteBishop(fromBitboard, targetBitboard); break;
				case WHITE_ROOK_BYTE:   moveWhiteRook  (fromBitboard, targetBitboard); break;
				case WHITE_QUEEN_BYTE:  moveWhiteQueen (fromBitboard, targetBitboard); break;
				case WHITE_KING_BYTE:   moveWhiteKing  (fromBitboard, targetBitboard, isApply); break;
			}
			recalculateWhiteOccupiedSquares();
		} else {
			switch (coloredMovingPiece) {
				case BLACK_PAWN_BYTE:   moveBlackPawn(fromBitboard, targetBitboard, move, isApply); break;
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
					black_pawns   ^= targetBitboard; break;
				case BLACK_KNIGHT_BYTE:
					pieces[targetIdx] = BLACK_KNIGHT_BYTE;
					black_knights ^= targetBitboard; break;
				case BLACK_BISHOP_BYTE:
					pieces[targetIdx] = BLACK_BISHOP_BYTE;
					black_bishops ^= targetBitboard; break;
				case BLACK_ROOK_BYTE:
					pieces[targetIdx] = BLACK_ROOK_BYTE;
					takeBlackRook(targetBitboard); break;
				case BLACK_QUEEN_BYTE:
					pieces[targetIdx] = BLACK_QUEEN_BYTE;
					black_queens  ^= targetBitboard; break;
				case BLACK_KING_BYTE:
					pieces[targetIdx] = BLACK_KING_BYTE;
					black_kings   ^= targetBitboard; break;
			}
			// TODO: Move this so we don't have to do the if statement
			recalculateBlackOccupiedSquares();
		} else {
			switch (takePiece) {
				case NO_PIECE:
					return;
				case WHITE_PAWN_BYTE:
					pieces[targetIdx] = WHITE_PAWN_BYTE;
					white_pawns   ^= targetBitboard; break;
				case WHITE_KNIGHT_BYTE:
					pieces[targetIdx] = WHITE_KNIGHT_BYTE;
					white_knights ^= targetBitboard; break;
				case WHITE_BISHOP_BYTE:
					pieces[targetIdx] = WHITE_BISHOP_BYTE;
					white_bishops ^= targetBitboard; break;
				case WHITE_ROOK_BYTE:
					pieces[targetIdx] = WHITE_ROOK_BYTE;
					takeWhiteRook(targetBitboard); break;
				case WHITE_QUEEN_BYTE:
					pieces[targetIdx] = WHITE_QUEEN_BYTE;
					white_queens  ^= targetBitboard; break;
				case WHITE_KING_BYTE:
					pieces[targetIdx] = WHITE_PAWN_BYTE;
					white_kings   ^= targetBitboard; break;
			}
			recalculateWhiteOccupiedSquares();
		}
		breakpoint();
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
		white_bishops ^= fromBitboard ^ targetBitboard;
	}

	private void moveBlackBishop(final long fromBitboard, final long targetBitboard) {
		black_bishops ^= fromBitboard ^ targetBitboard;
	}

	private void moveBlackKnight(final long fromBitboard, final long targetBitboard) {
		black_knights ^= fromBitboard ^ targetBitboard;
	}

	private void moveWhiteKnight(final long fromBitboard, final long targetBitboard) {
		white_knights ^= fromBitboard ^ targetBitboard;
	}

	private void moveWhiteRook(final long fromBitboard, final long targetBitboard) {
		if(fromBitboard == a1Bitboard) {
			white_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h1Bitboard) {
			white_king_or_rook_king_side_moved = true;
		}
		white_rooks ^= fromBitboard ^ targetBitboard;
	}

	private void moveBlackRook(final long fromBitboard, final long targetBitboard) {
		if(fromBitboard == a8Bitboard) {
			black_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h8Bitboard) {
			black_king_or_rook_king_side_moved = true;
		}
		black_rooks ^= fromBitboard ^ targetBitboard;
	}

	private void moveBlackQueen(final long fromBitboard, final long targetBitboard) {
		black_queens ^= fromBitboard ^ targetBitboard;
	}

	private void moveWhiteQueen(final long fromBitboard, final long targetBitboard) {
		white_queens ^= fromBitboard ^ targetBitboard;
	}

	private void moveWhiteKing(final long fromBitboard, final long targetBitboard, final boolean isApply) {
        white_kings ^= fromBitboard ^ targetBitboard;

		if (isApply) {
			if (fromBitboard == e1Bitboard) {
				white_king_or_rook_king_side_moved = true;
				white_king_or_rook_queen_side_moved = true;
				if (targetBitboard == c1Bitboard) {
					white_rooks = white_rooks ^ a1Bitboard ^ d1Bitboard;
					pieces[d1FieldIdx] = WHITE_ROOK_BYTE;
					pieces[a1FieldIdx] = NO_PIECE;
				} else if (targetBitboard == g1Bitboard) {
					white_rooks = white_rooks ^ h1Bitboard ^ f1Bitboard;
					pieces[f1FieldIdx] = WHITE_ROOK_BYTE;
					pieces[h1FieldIdx] = NO_PIECE;
				}
			}
		} else {
			if (targetBitboard == e1Bitboard) {
				if (fromBitboard == c1Bitboard) {
					white_king_or_rook_queen_side_moved = true;
					white_rooks = white_rooks ^ a1Bitboard ^ d1Bitboard;
					pieces[a1FieldIdx] = WHITE_ROOK_BYTE;
					pieces[d1FieldIdx] = NO_PIECE;
				} else if (fromBitboard == g1Bitboard) {
					white_king_or_rook_king_side_moved = true;
					white_rooks = white_rooks ^ h1Bitboard ^ f1Bitboard;
					pieces[h1FieldIdx] = WHITE_ROOK_BYTE;
					pieces[f1FieldIdx] = NO_PIECE;
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
				black_king_or_rook_king_side_moved = true;
				black_king_or_rook_queen_side_moved = true;
				if (targetBitboard == c8Bitboard) {
					black_rooks = black_rooks ^ a8Bitboard ^ d8Bitboard;
					pieces[d8FieldIdx] = BLACK_ROOK_BYTE;
					pieces[a8FieldIdx] = NO_PIECE;
				} else if (targetBitboard == g8Bitboard) {
					black_rooks = black_rooks ^ h8Bitboard ^ f8Bitboard;
					pieces[f8FieldIdx] = BLACK_ROOK_BYTE;
					pieces[h8FieldIdx] = NO_PIECE;
				}
			}
		} else {
			if (targetBitboard == e8Bitboard) {
				if (fromBitboard == c8Bitboard) {
					black_king_or_rook_queen_side_moved = false;
					black_rooks = black_rooks ^ a8Bitboard ^ d8Bitboard;
					pieces[a8FieldIdx] = BLACK_ROOK_BYTE;
					pieces[d8FieldIdx] = NO_PIECE;
				} else if (fromBitboard == g8Bitboard) {
					black_king_or_rook_king_side_moved = false;
					black_rooks = black_rooks ^ h8Bitboard ^ f8Bitboard;
					pieces[h8FieldIdx] = BLACK_ROOK_BYTE;
					pieces[f8FieldIdx] = NO_PIECE;
				}
			}
		}
		System.out.println("yY");
	}

	public void finalizeBitboards() {
		occupiedSquares[WHITE] = white_pawns | white_knights | white_bishops | white_rooks | white_queens | white_kings;
		occupiedSquares[BLACK] = black_pawns | black_knights | black_bishops | black_rooks | black_queens | black_kings;

		occupied_board = occupiedSquares[WHITE] | occupiedSquares[BLACK];
		unoccupied_board = ~occupied_board;
		enemy_and_empty_board = occupiedSquares[opponent(toMove)] | unoccupied_board;

		computeZobristHash();
	}

	public void finalizeBitboardsAfterApply(final int fromIdx, final int targetIdx,
											 final byte movingPiece, final byte takenPiece) {
		mutateGeneralBoardOccupation();
		mutateZobristHash(fromIdx, targetIdx, movingPiece, takenPiece);
	}

	public void mutateGeneralBoardOccupation() {
		// TODO: Test this hashing
		occupied_board = occupiedSquares[WHITE] | occupiedSquares[BLACK];
		unoccupied_board = ~occupied_board;
		byte opponent = opponent(toMove);
		enemy_and_empty_board = occupiedSquares[opponent] | unoccupied_board;
	}

	private void mutateZobristHash(int fromIdx, int targetIdx, byte movingPiece, byte takenPiece) {
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
		checkConsistency();
		return string(Predicates.alwaysTrue());
	}

	private String string(Predicate<Byte> predicate) {
		StringBuilder sb = new StringBuilder();
		for (byte fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			if((fieldIdx)%8 == 0) {
				sb.append('\n');
			}

			byte coloredPieceOnField = coloredPiece(fieldIdx);
			if(coloredPieceOnField == NO_PIECE || !predicate.apply(coloredPieceOnField)) {
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

	private byte[] coloredPiecePerField() {
		byte[] coloredPieces = new byte[64];
		for (byte fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			coloredPieces[fieldIdx] = coloredPiece(fieldIdx);
		}
		return coloredPieces;
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

	public boolean canTakeKing() {
		try {
			AceTakeMoveGenerator.generateTakeMoves(this);
			return false;
		} catch (KingEatingException e) {
			return true;
		}
	}

	public int getRepeatedMove() {
		return repeatedMove;
	}

//	public int getFiftyMove() {
//		// TODO: Implement using fiftyMove
//		return 0;
//	}

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

	public byte[] getPieces() {
		return pieces;
	}

	public boolean breakpoint() {
		return ACEBoardUtils.stringDump(this).contains("♚♜\n");
	}
}
