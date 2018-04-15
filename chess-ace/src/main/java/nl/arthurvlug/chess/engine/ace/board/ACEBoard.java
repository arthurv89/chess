package nl.arthurvlug.chess.engine.ace.board;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.board.pieces.PieceUtils;
import org.slf4j.LoggerFactory;

import static nl.arthurvlug.chess.engine.ColorUtils.*;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;

public class ACEBoard extends AbstractEngineBoard<UnapplyableMove> {
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
	private static int[][][] zobristRandomTable = new int[64][6][2];
	static {
		final Random random = new Random(1);
		for (int fieldIndex = 0; fieldIndex<64; fieldIndex++) {
			for (int piece = 0; piece<PieceType.values().length; piece++) {
				for (int color = 0; color<2; color++) {
					zobristRandomTable[fieldIndex][piece][color] = random.nextInt();
				}
			}
		}
	}


	protected ACEBoard() { }

	public void apply(List<String> moveList) {
		if(moveList.isEmpty()) {
			return;
		}
		
		Color currentToMove = Color.WHITE;
		for(String sMove : moveList) {
			UnapplyableMove move = UnapplyableMoveUtils.toMove(sMove, this);
			ColoredPiece movingPiece = pieceAt(move.getFrom());
			if(movingPiece == null) {
				throw new RuntimeException("Could not determine moving piece");
			}
			
			if(LoggerFactory.getLogger(getClass()).isDebugEnabled()) {
				if(currentToMove != movingPiece.getColor()) {
					System.out.println("Not correct?");
				}
				currentToMove = currentToMove.other();
			}
			apply(move);
		}
//		throw new UnsupportedOperationException();
	}

	public ColoredPiece pieceAt(Coordinates from) {
		return pieceAt(FieldUtils.fieldToString(from));
	}

	public ColoredPiece pieceAt(String fieldName) {
		return pieceAt(FieldUtils.fieldIdx(fieldName));
	}

	public ColoredPiece pieceAt(long fieldIdx) {
		long bitboard = 1L << fieldIdx;

		if((white_bishops & bitboard) != 0) return new ColoredPiece(PieceType.BISHOP, Color.WHITE);
		if((white_kings & bitboard) != 0)   return new ColoredPiece(PieceType.KING,   Color.WHITE);
		if((white_knights & bitboard) != 0) return new ColoredPiece(PieceType.KNIGHT, Color.WHITE);
		if((white_pawns & bitboard) != 0)   return new ColoredPiece(PieceType.PAWN,   Color.WHITE);
		if((white_queens & bitboard) != 0)  return new ColoredPiece(PieceType.QUEEN,  Color.WHITE);
		if((white_rooks & bitboard) != 0)   return new ColoredPiece(PieceType.ROOK,   Color.WHITE);
		
		if((black_bishops & bitboard) != 0) return new ColoredPiece(PieceType.BISHOP, Color.BLACK);
		if((black_kings & bitboard) != 0)   return new ColoredPiece(PieceType.KING,   Color.BLACK);
		if((black_knights & bitboard) != 0) return new ColoredPiece(PieceType.KNIGHT, Color.BLACK);
		if((black_pawns & bitboard) != 0)   return new ColoredPiece(PieceType.PAWN,   Color.BLACK);
		if((black_queens & bitboard) != 0)  return new ColoredPiece(PieceType.QUEEN,  Color.BLACK);
		if((black_rooks & bitboard) != 0)   return new ColoredPiece(PieceType.ROOK,   Color.BLACK);
		
		return null;
	}

	public void apply(UnapplyableMove move) {
		int toIdx = FieldUtils.fieldIdx(move.getTo());
		long toBitboard = 1L << toIdx;

		int fromIdx = FieldUtils.fieldIdx(move.getFrom());
		long fromBitboard = 1L << fromIdx;

		Preconditions.checkNotNull(pieceAt(fromIdx), String.format("Can't apply move %s: the from field is empty:\n%s", move, this.toString()));
		PieceType movingPiece = pieceAt(fromIdx).getPieceType();

		xorMove(toBitboard, fromBitboard, movingPiece);
		xorTakePiece(move, toBitboard);

		this.toMove = opponent(this.toMove);
		finalizeBitboardsAfterApply(fromIdx, toIdx, movingPiece, move.getTakePiece());
	}

	@Override
	public void unapply(final UnapplyableMove move) {
		this.toMove = opponent(this.toMove);

		int toIdx = FieldUtils.fieldIdx(move.getTo());
		long toBitboard = 1L << toIdx;
		final PieceType movingPiece = pieceAt(toIdx).getPieceType();

		int fromIdx = FieldUtils.fieldIdx(move.getFrom());
		long fromBitboard = 1L << fromIdx;

		// This is a reverse move
		xorMove(fromBitboard, toBitboard, movingPiece);

		xorTakePiece(move, toBitboard);

		finalizeBitboardsAfterApply(fromIdx, toIdx, movingPiece, move.getTakePiece());
	}

	private void xorMove(final long toBitboard, final long fromBitboard, final PieceType movingPiece) {
		if(isWhite(toMove)) {
			switch (movingPiece) {
				case PAWN:   moveWhitePawn  (fromBitboard, toBitboard); break;
				case KNIGHT: moveWhiteKnight(fromBitboard, toBitboard); break;
				case BISHOP: moveWhiteBishop(fromBitboard, toBitboard); break;
				case ROOK:   moveWhiteRook  (fromBitboard, toBitboard); break;
				case QUEEN:  moveWhiteQueen (fromBitboard, toBitboard); break;
				case KING:   moveWhiteKing  (fromBitboard, toBitboard); break;
			}
			recalculateWhiteOccupiedSquares();
		} else {
			switch (movingPiece) {
				case PAWN:   moveBlackPawn  (fromBitboard, toBitboard); break;
				case KNIGHT: moveBlackKnight(fromBitboard, toBitboard); break;
				case BISHOP: moveBlackBishop(fromBitboard, toBitboard); break;
				case ROOK:   moveBlackRook  (fromBitboard, toBitboard); break;
				case QUEEN:  moveBlackQueen (fromBitboard, toBitboard); break;
				case KING:   moveBlackKing  (fromBitboard, toBitboard); break;
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

	private void xorTakePiece(final UnapplyableMove move, final long toBitboard) {
		if(move.getTakePiece() != null) {
			if(isWhite(toMove)) {
				switch (move.getTakePiece().getPieceType()) {
					case PAWN:   black_pawns   ^= toBitboard; break;
					case KNIGHT: black_knights ^= toBitboard; break;
					case BISHOP: black_bishops ^= toBitboard; break;
					case ROOK:   black_rooks   ^= toBitboard; break;
					case QUEEN:  black_queens  ^= toBitboard; break;
					case KING:   black_kings   ^= toBitboard; break;
				}
				recalculateBlackOccupiedSquares();
			} else {
				switch (move.getTakePiece().getPieceType()) {
					case PAWN:   white_pawns   ^= toBitboard; break;
					case KNIGHT: white_knights ^= toBitboard; break;
					case BISHOP: white_bishops ^= toBitboard; break;
					case ROOK:   white_rooks   ^= toBitboard; break;
					case QUEEN:  white_queens  ^= toBitboard; break;
					case KING:   white_kings   ^= toBitboard; break;
				}
				recalculateWhiteOccupiedSquares();
			}
		}
	}

	private void moveWhitePawn(final long fromBitboard, final long toBitboard) {
		white_pawns ^= fromBitboard;
		white_pawns ^= toBitboard;
	}

	private void moveBlackPawn(final long fromBitboard, final long toBitboard) {
		black_pawns ^= fromBitboard;
		black_pawns ^= toBitboard;
	}

	private void moveWhiteBishop(final long fromBitboard, final long toBitboard) {
		white_bishops ^= fromBitboard;
		white_bishops ^= toBitboard;
	}

	private void moveBlackBishop(final long fromBitboard, final long toBitboard) {
		black_bishops ^= fromBitboard;
		black_bishops ^= toBitboard;
	}

	private void moveBlackKnight(final long fromBitboard, final long toBitboard) {
		black_knights ^= fromBitboard;
		black_knights ^= toBitboard;
	}

	private void moveWhiteKnight(final long fromBitboard, final long toBitboard) {
		white_knights ^= fromBitboard;
		white_knights ^= toBitboard;
	}

	private void moveWhiteRook(final long fromBitboard, final long toBitboard) {
		if(fromBitboard == a1Bitboard) {
			white_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h1Bitboard) {
			white_king_or_rook_king_side_moved = true;
		}
		white_rooks ^= fromBitboard;
		white_rooks ^= toBitboard;
	}

	private void moveBlackRook(final long fromBitboard, final long toBitboard) {
		if(fromBitboard == a8Bitboard) {
			black_king_or_rook_queen_side_moved = true;
		}
		else if(fromBitboard == h8Bitboard) {
			black_king_or_rook_king_side_moved = true;
		}
		black_rooks ^= fromBitboard;
		black_rooks ^= toBitboard;
	}

	private void moveBlackQueen(final long fromBitboard, final long toBitboard) {
		black_queens ^= fromBitboard;
		black_queens ^= toBitboard;
	}

	private void moveWhiteQueen(final long fromBitboard, final long toBitboard) {
		white_queens ^= fromBitboard;
		white_queens ^= toBitboard;
	}

	private void moveWhiteKing(final long fromBitboard, final long toBitboard) {
		white_king_or_rook_queen_side_moved = true;
		white_king_or_rook_king_side_moved = true;
		white_kings ^= fromBitboard;
		white_kings ^= toBitboard;
		if(fromBitboard == e1Bitboard) {
			if (toBitboard == c1Bitboard) {
				this.white_rooks ^= a1Bitboard;
				this.white_rooks ^= d1Bitboard;
			} else if (toBitboard == g1Bitboard) {
				this.white_rooks ^= h1Bitboard;
				this.white_rooks ^= f1Bitboard;
			}
		}
	}

	private void moveBlackKing(final long fromBitboard, final long toBitboard) {
		black_king_or_rook_queen_side_moved = true;
		black_king_or_rook_king_side_moved = true;
		black_kings ^= fromBitboard;
		black_kings ^= toBitboard;

		if (fromBitboard == e8Bitboard) {
			if (toBitboard == c8Bitboard) {
				this.black_rooks ^= a8Bitboard;
				this.black_rooks ^= d8Bitboard;
			} else if (toBitboard == g8Bitboard) {
				this.black_rooks ^= h8Bitboard;
				this.black_rooks ^= f8Bitboard;
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

	private void finalizeBitboardsAfterApply(final int fromIdx, final int toIdx,
											 final PieceType movingPiece, final ColoredPiece takenPiece) {
		occupied_board = occupiedSquares[WHITE] | occupiedSquares[BLACK];
		unoccupied_board = ~occupied_board;
		byte opponent = opponent(toMove);
		enemy_and_empty_board = occupiedSquares[opponent] | unoccupied_board;

		// TODO: Test this hashing
		zobristHash ^= bitsToSwap(fromIdx, movingPiece, opponent);
		zobristHash ^= bitsToSwap(toIdx, movingPiece, opponent);
		if(takenPiece != null) {
			zobristHash ^= bitsToSwap(toIdx, takenPiece.getPieceType(), toMove);
		}
	}

	// Only to be calculated once. After that, it should recalculate the hash using incremental updates
	private void computeZobristHash() {
		zobristHash = 1659068882;
		for (int fieldIdx = 0; fieldIdx<64; fieldIdx++) {
			ColoredPiece coloredPiece = pieceAt(fieldIdx);
			if(coloredPiece != null) {
				zobristHash ^= bitsToSwap(fieldIdx, coloredPiece.getPieceType(), coloredPiece.getColor().ordinal());
			}
		}
	}

	private long bitsToSwap(final int fieldIdx, final PieceType pieceType, final int pieceColor) {
		return zobristPieceHash(fieldIdx, pieceType, pieceColor);
	}

	private int zobristPieceHash(final int fieldIdx, final PieceType pieceType, final int pieceColor) {
		int pieceTypeInt = pieceType.ordinal();
		return zobristRandomTable[fieldIdx][pieceTypeInt][pieceColor];
	}

	@Override
	public List<UnapplyableMove> generateTakeMoves() {
		return AceMoveGenerator.generateMoves(this)
				.stream()
				.filter(move  -> {
					int idx = FieldUtils.fieldIdx(move.getTo());
					return pieceAt(idx) != null;
				})
				.collect(Collectors.toList());
	}

	public String string() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		Preconditions.checkArgument((occupiedSquares[WHITE] & occupiedSquares[BLACK]) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.toBitboardString((occupiedSquares[WHITE] & occupiedSquares[BLACK])));
		
		StringBuilder sb = new StringBuilder();
		for (int fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			ColoredPiece pieceAt = pieceAt(FieldUtils.coordinates(fieldIdx));
			
			if((fieldIdx)%8 == 0) {
				sb.append('\n');
			}
			
			if(pieceAt == null) {
				sb.append('.');
			} else {
				String c = PieceUtils.toCharacterString(pieceAt, PieceUtils.pieceToChessSymbolMap);
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
		return clonedBoard;
	}

	@Override
	public List<UnapplyableMove> generateMoves() {
		return AceMoveGenerator.generateMoves(this);
	}

	@Override
	public int getRepeatedMove() {
		return repeatedMove;
	}

	@Override
	public int getFiftyMove() {
		return fiftyMove;
	}

	@Override
	public int getToMove() {
		return toMove;
	}

	@Override
	public boolean hasNoKing() {
		if(isWhite(toMove)) {
			return white_kings == 0;
		} else {
			return black_kings == 0;
		}
	}

	@Override
	public int getZobristHash() {
		return zobristHash;
	}
}
