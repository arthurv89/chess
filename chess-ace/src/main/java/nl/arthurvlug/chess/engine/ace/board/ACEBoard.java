package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import static nl.arthurvlug.chess.engine.ColorUtils.*;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;
import static nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration.DEBUG;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;

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
	private static final long e8Bitboard = bitboardFromFieldName("e8");

	// White castling positions
	private static final long a1Bitboard = bitboardFromFieldName("a1");
	private static final long d1Bitboard = bitboardFromFieldName("d1");
	private static final long f1Bitboard = bitboardFromFieldName("f1");
	private static final long h1Bitboard = bitboardFromFieldName("h1");
	// King positions
	private static final long g1Bitboard = bitboardFromFieldName("g1");
	private static final long c1Bitboard = bitboardFromFieldName("c1");
	private static final long e1Bitboard = bitboardFromFieldName("e1");

	private int zobristHash;
	private static int[][] zobristRandomTable = new int[64][13];
	static {
		final Random random = new Random(1);
		for (int fieldIndex = 0; fieldIndex<64; fieldIndex++) {
			for (byte coloredPieceType = 0; coloredPieceType< values().length; coloredPieceType++) {
				for (int color = 0; color<2; color++) {
					zobristRandomTable[fieldIndex][coloredPieceType] = random.nextInt();
				}
			}
		}
	}

	private byte[] piecesArray;


	protected ACEBoard() { }

	public void apply(List<String> moveList) {
		if(moveList.isEmpty()) {
			return;
		}
		
		for(String sMove : moveList) {
			int move = UnapplyableMoveUtils.createMove(sMove, this);
			byte movingPiece = UnapplyableMove.movingPiece(move);
			if(DEBUG && movingPiece == NO_PIECE) {
				throw new RuntimeException("Could not determine moving piece while executing " + UnapplyableMoveUtils.toString(move));
			}
			apply(move);
		}
//		throw new UnsupportedOperationException();
	}

	public short coloredPiece(String fieldName) {
		return coloredPiece(FieldUtils.fieldIdx(fieldName));
	}

	public byte coloredPiece(byte fieldIdx) {
		long bitboard = 1L << fieldIdx;

		if((white_pawns & bitboard) != 0)   return WHITE_PAWN_BYTE;
		if((white_knights & bitboard) != 0) return WHITE_KNIGHT_BYTE;
		if((white_bishops & bitboard) != 0) return WHITE_BISHOP_BYTE;
		if((white_rooks & bitboard) != 0)   return WHITE_ROOK_BYTE;
		if((white_queens & bitboard) != 0)  return WHITE_QUEEN_BYTE;
		if((white_kings & bitboard) != 0)   return WHITE_KING_BYTE;

		if((black_pawns & bitboard) != 0)   return BLACK_PAWN_BYTE;
		if((black_knights & bitboard) != 0) return BLACK_KNIGHT_BYTE;
		if((black_bishops & bitboard) != 0) return BLACK_BISHOP_BYTE;
		if((black_rooks & bitboard) != 0)   return BLACK_ROOK_BYTE;
		if((black_queens & bitboard) != 0)  return BLACK_QUEEN_BYTE;
		if((black_kings & bitboard) != 0)   return BLACK_KING_BYTE;

		return NO_PIECE;
	}

	public void apply(int move) {
		// TODO: Fix this by creating a more efficient Move object
		byte fromIdx = UnapplyableMove.fromIdx(move);
		long fromBitboard = 1L << fromIdx;

		byte targetIdx = UnapplyableMove.targetIdx(move);
		long targetBitboard = 1L << targetIdx;

		if(DEBUG) {
			Preconditions.checkNotNull(FieldUtils.fieldToString(fromIdx), String.format("Can't apply move %s: the from field is empty:\n%s", move, this.toString()));
		}
		byte movingPiece = UnapplyableMove.movingPiece(move);

		xorMove(targetBitboard, fromBitboard, movingPiece, true);
		xorTakePiece(move, targetBitboard);

		this.toMove = opponent(this.toMove);
		finalizeBitboardsAfterApply(fromIdx, targetIdx, movingPiece, UnapplyableMove.takePiece(move));
	}

	public void unapply(final int move) {
		this.toMove = opponent(this.toMove);

		byte targetIdx = UnapplyableMove.targetIdx(move);
		long targetBitboard = 1L << targetIdx;
		final byte movingPiece = UnapplyableMove.movingPiece(move);

		byte fromIdx = UnapplyableMove.fromIdx(move);
		long fromBitboard = 1L << fromIdx;

		// This is a reverse move
		xorMove(fromBitboard, targetBitboard, movingPiece, false);

		xorTakePiece(move, targetBitboard);

		finalizeBitboardsAfterApply(fromIdx, targetIdx, movingPiece, UnapplyableMove.takePiece(move));
	}

	private void xorMove(final long targetBitboard, final long fromBitboard, final short movingPiece, final boolean isApply) {
		// TODO: Remove if statement
		if(isWhite(toMove)) {
			switch (movingPiece) {
				case WHITE_PAWN_BYTE:   moveWhitePawn  (fromBitboard, targetBitboard); break;
				case WHITE_KNIGHT_BYTE: moveWhiteKnight(fromBitboard, targetBitboard); break;
				case WHITE_BISHOP_BYTE: moveWhiteBishop(fromBitboard, targetBitboard); break;
				case WHITE_ROOK_BYTE:   moveWhiteRook  (fromBitboard, targetBitboard); break;
				case WHITE_QUEEN_BYTE:  moveWhiteQueen (fromBitboard, targetBitboard); break;
				case WHITE_KING_BYTE:   moveWhiteKing  (fromBitboard, targetBitboard, isApply); break;
			}
			recalculateWhiteOccupiedSquares();
		} else {
			switch (movingPiece) {
				case BLACK_PAWN_BYTE:   moveBlackPawn  (fromBitboard, targetBitboard); break;
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

	private void xorTakePiece(final int move, final long targetBitboard) {
		short takePiece = UnapplyableMove.takePiece(move);
		if(isWhite(toMove)) {
			switch (takePiece) {
				case NO_PIECE: return;
				case BLACK_PAWN_BYTE:   black_pawns   ^= targetBitboard; break;
				case BLACK_KNIGHT_BYTE: black_knights ^= targetBitboard; break;
				case BLACK_BISHOP_BYTE: black_bishops ^= targetBitboard; break;
				case BLACK_ROOK_BYTE:   black_rooks   ^= targetBitboard; break;
				case BLACK_QUEEN_BYTE:  black_queens  ^= targetBitboard; break;
				case BLACK_KING_BYTE:   black_kings   ^= targetBitboard; break;
			}
			// TODO: Move this so we don't have to do the if statement
			recalculateBlackOccupiedSquares();
		} else {
			switch (takePiece) {
				case NO_PIECE: return;
				case WHITE_PAWN_BYTE:   white_pawns   ^= targetBitboard; break;
				case WHITE_KNIGHT_BYTE: white_knights ^= targetBitboard; break;
				case WHITE_BISHOP_BYTE: white_bishops ^= targetBitboard; break;
				case WHITE_ROOK_BYTE:   white_rooks   ^= targetBitboard; break;
				case WHITE_QUEEN_BYTE:  white_queens  ^= targetBitboard; break;
				case WHITE_KING_BYTE:   white_kings   ^= targetBitboard; break;
			}
			recalculateWhiteOccupiedSquares();
		}
	}

	private void moveWhitePawn(final long fromBitboard, final long targetBitboard) {
		white_pawns ^= fromBitboard;
		white_pawns ^= targetBitboard;
	}

	private void moveBlackPawn(final long fromBitboard, final long targetBitboard) {
		black_pawns ^= fromBitboard;
		black_pawns ^= targetBitboard;
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
		white_king_or_rook_queen_side_moved = true;
		white_king_or_rook_king_side_moved = true;
		white_kings ^= fromBitboard;
		white_kings ^= targetBitboard;
		if(isApply && fromBitboard == e1Bitboard || !isApply && targetBitboard == e1Bitboard) {
			if (targetBitboard == c1Bitboard) {
				this.white_rooks ^= a1Bitboard;
				this.white_rooks ^= d1Bitboard;
			} else if (targetBitboard == g1Bitboard) {
				this.white_rooks ^= h1Bitboard;
				this.white_rooks ^= f1Bitboard;
			}
		}
	}

	private void moveBlackKing(final long fromBitboard, final long targetBitboard, final boolean isApply) {
		black_king_or_rook_queen_side_moved = true;
		black_king_or_rook_king_side_moved = true;
		black_kings ^= fromBitboard;
		black_kings ^= targetBitboard;

		if(isApply) {
			if (fromBitboard == e8Bitboard) {
				if (targetBitboard == c8Bitboard) {
					this.black_rooks ^= a8Bitboard;
					this.black_rooks ^= d8Bitboard;
				} else if (targetBitboard == g8Bitboard) {
					this.black_rooks ^= h8Bitboard;
					this.black_rooks ^= f8Bitboard;
				}
			}
		} else {
			if (targetBitboard == e8Bitboard) {
				if (fromBitboard == c8Bitboard) {
					this.black_rooks ^= a8Bitboard;
					this.black_rooks ^= d8Bitboard;
				} else if (fromBitboard == g8Bitboard) {
					this.black_rooks ^= h8Bitboard;
					this.black_rooks ^= f8Bitboard;
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

	public List<Integer> generateTakeMoves() {
		return AceMoveGenerator.generateMoves(this)
				.stream()
				.filter(move -> {
					byte idx = UnapplyableMove.targetIdx(move);
					return coloredPiece(idx) != NO_PIECE;
				})
				.collect(Collectors.toList());
	}

	public String string() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		Preconditions.checkArgument((occupiedSquares[WHITE] & occupiedSquares[BLACK]) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.targetBitboardString((occupiedSquares[WHITE] & occupiedSquares[BLACK])));
		
		StringBuilder sb = new StringBuilder();
		for (byte fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
//			ColoredPiece pieceAt = (fieldIdx);
			
			if((fieldIdx)%8 == 0) {
				sb.append('\n');
			}

			byte pieceOnField = coloredPiece(fieldIdx);
			if(pieceOnField == NO_PIECE) {
				sb.append('.');
			} else {
				String c = PieceStringUtils.toCharacterString(ColoredPieceType.from(pieceOnField), PieceStringUtils.pieceToChessSymbolMap);
				sb.append(c);
			}
		}
		String reversedBoard = sb.toString();
		
		// Reverse rows
		List<String> l = Lists.newArrayList(Splitter.on('\n').split(reversedBoard));
		Collections.reverse(l);
		return Joiner.on('\n').join(l);
	}

	public void addPiece(int engineColor, PieceType pieceType, int fieldIndex) {
		if(PieceType.KING == pieceType) {
			if(engineColor == WHITE) {
				white_kings |= 1L << fieldIndex;
			} else {
				black_kings |= 1L << fieldIndex;
			}
		}

		if(PieceType.QUEEN == pieceType) {
			if(engineColor == WHITE) {
				white_queens |= 1L << fieldIndex;
			} else {
				black_queens |= 1L << fieldIndex;
			}
		}

		if(PieceType.ROOK == pieceType) {
			if(engineColor == WHITE) {
				white_rooks |= 1L << fieldIndex;
			} else {
				black_rooks |= 1L << fieldIndex;
			}
		}

		if(PieceType.BISHOP == pieceType) {
			if(engineColor == WHITE) {
				white_bishops |= 1L << fieldIndex;
			} else {
				black_bishops |= 1L << fieldIndex;
			}
		}

		if(PieceType.KNIGHT == pieceType) {
			if(engineColor == WHITE) {
				white_knights |= 1L << fieldIndex;
			} else {
				black_knights |= 1L << fieldIndex;
			}
		}

		if(PieceType.PAWN == pieceType) {
			if(engineColor == WHITE) {
				white_pawns |= 1L << fieldIndex;
			} else {
				black_pawns |= 1L << fieldIndex;
			}
		}
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
		clonedBoard.black_kings = this.black_kings;
		clonedBoard.white_kings = this.white_kings;
		clonedBoard.black_queens = this.black_queens;
		clonedBoard.white_queens = this.white_queens;
		clonedBoard.white_rooks = this.white_rooks;
		clonedBoard.black_rooks = this.black_rooks;
		clonedBoard.white_bishops = this.white_bishops;
		clonedBoard.black_bishops = this.black_bishops;
		clonedBoard.white_knights = this.white_knights;
		clonedBoard.black_knights = this.black_knights;
		clonedBoard.white_pawns = this.white_pawns;
		clonedBoard.black_pawns = this.black_pawns;

		clonedBoard.white_king_or_rook_queen_side_moved = this.white_king_or_rook_queen_side_moved;
		clonedBoard.white_king_or_rook_king_side_moved = this.white_king_or_rook_king_side_moved;
		clonedBoard.black_king_or_rook_queen_side_moved = this.black_king_or_rook_queen_side_moved;
		clonedBoard.black_king_or_rook_king_side_moved = this.black_king_or_rook_king_side_moved;

		clonedBoard.toMove = this.toMove;
		clonedBoard.zobristHash = 0;
		clonedBoard.finalizeBitboards();
		return clonedBoard;
	}

	public List<Integer> generateMoves() {
		return AceMoveGenerator.generateMoves(this);
	}

	public int getRepeatedMove() {
		return repeatedMove;
	}

	public int getFiftyMove() {
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
}
