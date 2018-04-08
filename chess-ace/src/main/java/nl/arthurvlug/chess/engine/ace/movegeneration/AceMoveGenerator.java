package nl.arthurvlug.chess.engine.ace.movegeneration;

import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg135_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg225_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg315_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg45_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.left_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_black_one_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_black_take_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_black_two_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_white_one_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_white_take_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.pawn_xray_white_two_field_move;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.right_board;
import static nl.arthurvlug.chess.utils.board.pieces.PieceType.KING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.arthurvlug.chess.utils.game.Move;

public class AceMoveGenerator {
	private static final int CASTLE_QUEEN_SIZE = 0;
	private static final int CASTLE_KING_SIZE = 1;

	public static boolean opponentIsInCheck(final ACEBoard engineBoard, final List<Move> generatedMoves) {
		for(Move move : generatedMoves) {
			final ColoredPiece takePiece = engineBoard.pieceAt(move.getTo());
			if(takePiece != null && takePiece.getPieceType() == KING) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Generates both valid moves and invalid moves
	 * @param engineBoard
	 * @return
	 */
	public static List<Move> generateMoves(ACEBoard engineBoard) {
		Preconditions.checkArgument(engineBoard.occupied_board != 0L);
		Preconditions.checkArgument(engineBoard.enemy_and_empty_board != 0L);
		
		ImmutableList<Move> validAndInvalidMoves = ImmutableList.<Move> builder()
			.addAll(knightMoves(engineBoard))
			.addAll(pawnMoves(engineBoard))
			.addAll(rookMoves(engineBoard))
			.addAll(bishopMoves(engineBoard))
			.addAll(queenMoves(engineBoard))
			.addAll(kingMoves(engineBoard))
			.addAll(castlingMoves(engineBoard))
			// TODO: Implement castling, en passent
			.build();
		
		List<Move> validMoves = new ArrayList<>();
		for (Move validOrInvalidMove : validAndInvalidMoves) {
			ACEBoard successorBoard = new ACEBoard(engineBoard);
			successorBoard.finalizeBitboards();
			successorBoard.apply(validOrInvalidMove);

			validMoves.add(validOrInvalidMove);
		}
		return validMoves;
	}

	private static List<Move> pawnMoves(ACEBoard engineBoard) {
		long[] pawnXrayOneFieldMove = engineBoard.toMove == EngineConstants.WHITE ? pawn_xray_white_one_field_move : pawn_xray_black_one_field_move;
		long[] pawnXrayTwoFieldMove = engineBoard.toMove == EngineConstants.WHITE ? pawn_xray_white_two_field_move : pawn_xray_black_two_field_move;;
		long[] pawnXrayTakeFieldMove = engineBoard.toMove == EngineConstants.WHITE ? pawn_xray_white_take_field_move : pawn_xray_black_take_field_move;;
		
		List<Move> moves = new ArrayList<>();
		long pawns = pawns(engineBoard);
		while(pawns != 0L) {
			int sq = Long.numberOfTrailingZeros(pawns);
			pawns -= 1L << sq;
			
			long oneFieldMove = pawnXrayOneFieldMove[sq] & engineBoard.unoccupied_board;
			moves.addAll(moves(sq, oneFieldMove));
			
			if(oneFieldMove != 0) {
				long twoFieldMove = pawnXrayTwoFieldMove[sq] & engineBoard.unoccupied_board;
				moves.addAll(moves(sq, twoFieldMove));
			}
			long twoFieldsMove = pawnXrayTakeFieldMove[sq] & engineBoard.enemy_board;
			moves.addAll(moves(sq, twoFieldsMove));
		}
		return moves;
	}

	private static List<Move> queenMoves(ACEBoard engineBoard) {
		List<Move> moves = new ArrayList<>();
		long queens = queens(engineBoard);
		while(queens != 0L) {
			int sq = Long.numberOfTrailingZeros(queens);

			long queen_perpendicular_moves = bishopMoves(engineBoard, sq);
			long queen_diagonal_moves = rookMoves(engineBoard, sq);

			queens -= 1L << sq;
			moves.addAll(moves(sq, queen_perpendicular_moves));
			moves.addAll(moves(sq, queen_diagonal_moves));
		}
		return moves;
	}

	private static List<Move> bishopMoves(ACEBoard engineBoard) {
		List<Move> moves = new ArrayList<>();
		long bishops = bishops(engineBoard);
		while(bishops != 0L) {
			int sq = Long.numberOfTrailingZeros(bishops);
			
			long bishop_moves = bishopMoves(engineBoard, sq);
			
			bishops -= 1L << sq;
			moves.addAll(moves(sq, bishop_moves));
		}
		return moves;
	}

	private static long bishopMoves(ACEBoard engineBoard, int sq) {
		long deg45_moves = deg45_board[sq] & engineBoard.occupied_board;
		deg45_moves = (deg45_moves<<9) | (deg45_moves<<18) | (deg45_moves<<27) | (deg45_moves<<36) | (deg45_moves<<45) | (deg45_moves<<54); 
		deg45_moves = deg45_moves & deg45_board[sq];
		deg45_moves = (deg45_moves ^ deg45_board[sq]) & engineBoard.enemy_and_empty_board;
		long deg225_moves = deg225_board[sq] & engineBoard.occupied_board;
		deg225_moves = (deg225_moves>>9) | (deg225_moves>>18) | (deg225_moves>>27) | (deg225_moves>>36) | (deg225_moves>>45) | (deg225_moves>>54); 
		deg225_moves = deg225_moves & deg225_board[sq];
		deg225_moves = (deg225_moves ^ deg225_board[sq]) & engineBoard.enemy_and_empty_board;
		long deg135_moves = deg135_board[sq] & engineBoard.occupied_board;
		deg135_moves = (deg135_moves<<7) | (deg135_moves<<14) | (deg135_moves<<21) | (deg135_moves<<28) | (deg135_moves<<35) | (deg135_moves<<42); 
		deg135_moves = deg135_moves & deg135_board[sq];
		deg135_moves = (deg135_moves ^ deg135_board[sq]) & engineBoard.enemy_and_empty_board;
		long deg315_moves = deg315_board[sq] & engineBoard.occupied_board;
		deg315_moves = (deg315_moves>>7) | (deg315_moves>>14) | (deg315_moves>>21) | (deg315_moves>>28) | (deg315_moves>>35) | (deg315_moves>>42); 
		deg315_moves = deg315_moves & deg315_board[sq];
		deg315_moves = (deg315_moves ^ deg315_board[sq]) & engineBoard.enemy_and_empty_board;
		long bishop_moves = deg45_moves | deg225_moves | deg135_moves | deg315_moves;
		return bishop_moves;
	}

	private static List<Move> rookMoves(ACEBoard engineBoard) {
		List<Move> moves = new ArrayList<>();
		long rooks = rooks(engineBoard);
		while(rooks != 0L) {
			int sq = Long.numberOfTrailingZeros(rooks);
			
			long rook_moves = rookMoves(engineBoard, sq);
			
			rooks -= 1L << sq;
			moves.addAll(moves(sq, rook_moves));
		}
		return moves;
	}

	private static long rookMoves(ACEBoard engineBoard, int sq) {
		long right_moves = right_board[sq] & engineBoard.occupied_board;
		right_moves = (right_moves<<1) | (right_moves<<2) | (right_moves<<3) | (right_moves<<4) | (right_moves<<5) | (right_moves<<6); 
		right_moves = right_moves & right_board[sq];
		right_moves = (right_moves ^ right_board[sq]) & engineBoard.enemy_and_empty_board;
		long left_moves = left_board[sq] & engineBoard.occupied_board;
		left_moves = (left_moves>>1) | (left_moves>>2) | (left_moves>>3) | (left_moves>>4) | (left_moves>>5) | (left_moves>>6); 
		left_moves = left_moves & left_board[sq];
		left_moves = (left_moves ^ Xray.left_board[sq]) & engineBoard.enemy_and_empty_board;
		long up_moves = Xray.up_board[sq] & engineBoard.occupied_board;
		up_moves = (up_moves<<8) | (up_moves<<16) | (up_moves<<24) | (up_moves<<32) | (up_moves<<40) | (up_moves<<48); 
		up_moves = up_moves & Xray.up_board[sq];
		up_moves = (up_moves ^ Xray.up_board[sq]) & engineBoard.enemy_and_empty_board;
		long down_moves = Xray.down_board[sq] & engineBoard.occupied_board;
		down_moves = (down_moves>>8) | (down_moves>>16) | (down_moves>>24) | (down_moves>>32) | (down_moves>>40) | (down_moves>>48); 
		down_moves = down_moves & Xray.down_board[sq];
		down_moves = (down_moves ^ Xray.down_board[sq]) & engineBoard.enemy_and_empty_board;
		long rook_moves = right_moves | left_moves | up_moves | down_moves;
		return rook_moves;
	}

	private static List<Move> knightMoves(ACEBoard engineBoard) {
		List<Move> moves = new ArrayList<>();
		long knights = knights(engineBoard);
		while(knights != 0L) {
			int sq = Long.numberOfTrailingZeros(knights);
			long destinationBitboard = Xray.knight_xray[sq] & engineBoard.enemy_and_empty_board;
			knights -= 1L << sq;
			moves.addAll(moves(sq, destinationBitboard));
		}
		return moves;
	}

	private static List<Move> kingMoves(ACEBoard engineBoard) {
		int sq = Long.numberOfTrailingZeros(kings(engineBoard));
		if(sq == 64) {
			return Collections.emptyList();
		}

		long destinationBitboard = Xray.king_xray[sq] & engineBoard.enemy_and_empty_board;
		return moves(sq, destinationBitboard);
	}

	static List<Move> castlingMoves(final ACEBoard engineBoard) {
		final List<Move> moves = new ArrayList<>();
		if(ColorUtils.isWhite(engineBoard.toMove)) {
			Coordinates fromCoordinate = BitboardUtils.coordinates(4);
			boolean canCastleQueenSide = canCastle(engineBoard, engineBoard.white_king_or_rook_queen_side_moved, Xray.castling_xray[engineBoard.toMove][CASTLE_QUEEN_SIZE]);
			boolean canCastleKingSide = canCastle(engineBoard, engineBoard.white_king_or_rook_king_side_moved, Xray.castling_xray[engineBoard.toMove][CASTLE_KING_SIZE]);
			if(canCastleQueenSide) {
				Coordinates toCoordinate = BitboardUtils.coordinates(2);
				Move move = new Move(fromCoordinate, toCoordinate, Optional.empty());
				moves.add(move);
			}
			if(canCastleKingSide) {
				Coordinates toCoordinate = BitboardUtils.coordinates(6);
				Move move = new Move(fromCoordinate, toCoordinate, Optional.empty());
				moves.add(move);
			}
		} else {
			Coordinates fromCoordinate = BitboardUtils.coordinates(60);
			boolean canCastleQueenSide = canCastle(engineBoard, engineBoard.black_king_or_rook_queen_side_moved, Xray.castling_xray[engineBoard.toMove][CASTLE_QUEEN_SIZE]);
			boolean canCastleKingSide = canCastle(engineBoard, engineBoard.black_king_or_rook_king_side_moved, Xray.castling_xray[engineBoard.toMove][CASTLE_KING_SIZE]);
			if(canCastleQueenSide) {
				Coordinates toCoordinate = BitboardUtils.coordinates(58);
				Move move = new Move(fromCoordinate, toCoordinate, Optional.empty());
				moves.add(move);
			}
			if(canCastleKingSide) {
				Coordinates toCoordinate = BitboardUtils.coordinates(62);
				Move move = new Move(fromCoordinate, toCoordinate, Optional.empty());
				moves.add(move);
			}
		}
		return ImmutableList.copyOf(moves);
	}

	/**
	 * Warning: Doesn't check whether the pieces are on the right place. If we do games that don't start with the starting
	 * position, we need to set the castle moved booleans in the ACEBoard class
	 */
	private static boolean canCastle(final ACEBoard engineBoard, final boolean piecesMoved, final long xRay) {
		return !piecesMoved
				&& (xRay & engineBoard.occupied_board) == 0L;
	}

	private static List<Move> moves(int index, long bitboard) {
		List<Move> moves = new ArrayList<>();
		Coordinates fromCoordinate = BitboardUtils.coordinates(index);
		
		while(bitboard != 0) {
			int onePos = Long.numberOfTrailingZeros(bitboard);
			Coordinates toCoordinate = BitboardUtils.coordinates(onePos);
			Move move = new Move(fromCoordinate, toCoordinate, Optional.empty());
			moves.add(move);
			
			bitboard -= 1L << onePos;
		}
		return moves;
	}
	
	private static long pawns(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_pawns
				: engineBoard.black_pawns;
	}

	private static long queens(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_queens
				: engineBoard.black_queens;
	}

	private static long bishops(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_bishops
				: engineBoard.black_bishops;
	}

	private static long rooks(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_rooks
				: engineBoard.black_rooks;
	}

	private static long kings(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_kings
				: engineBoard.black_kings;
	}

	private static long knights(ACEBoard engineBoard) {
		return engineBoard.toMove == EngineConstants.WHITE
				? engineBoard.white_knights
				: engineBoard.black_knights;
	}
}