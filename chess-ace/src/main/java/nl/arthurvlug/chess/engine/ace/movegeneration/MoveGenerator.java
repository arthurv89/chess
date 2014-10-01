package nl.arthurvlug.chess.engine.ace.movegeneration;

import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg135_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg225_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg315_board;
import static nl.arthurvlug.chess.engine.ace.movegeneration.Xray.deg45_board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableList;

public class MoveGenerator {
	public static List<AceMove> generateMoves(ACEBoard engineBoard) {
		return generateMoves(engineBoard, true);
	}
	
	public static List<AceMove> generateMoves(ACEBoard engineBoard, boolean validateMoves) {
		ImmutableList<AceMove> validAndInvalidMoves = ImmutableList.<AceMove> builder()
			.addAll(kingMoves(engineBoard))
			.addAll(knightMoves(engineBoard))
			.addAll(rookMoves(engineBoard))
			.addAll(bishopMoves(engineBoard))
			.addAll(queenMoves(engineBoard))
			// TODO: Implement pawn, castling, en passent
			.build();
		
		List<AceMove> validMoves = new ArrayList<>();
		engineBoard.successorBoards = new ArrayList<>();
		for (AceMove validOrInvalidMove : validAndInvalidMoves) {
			ACEBoard successorBoard = new ACEBoard(engineBoard);
			successorBoard.apply(validOrInvalidMove);
			
			boolean isValid = true;
			if(validateMoves) {
				generateMoves(successorBoard, false);
				for(ACEBoard successorSuccessorBoard : successorBoard.successorBoards) {
					successorSuccessorBoard.finalizeBitboards();
					if(successorSuccessorBoard.white_kings == 0L && engineBoard.toMove == EngineConstants.WHITE
							|| successorSuccessorBoard.black_kings == 0L && engineBoard.toMove == EngineConstants.BLACK) {
						isValid = false;
					}
				}
			}

			if(isValid) {
				validMoves.add(validOrInvalidMove);
				engineBoard.successorBoards.add(successorBoard);
			}
		}
		return validMoves;
	}

	private static List<AceMove> queenMoves(ACEBoard engineBoard) {
		List<AceMove> moves = new ArrayList<>();
		long queens = queens(engineBoard);
		while(queens != 0L) {
			int sq = Long.numberOfTrailingZeros(queens);

			long queen_perpendicular_moves = bishopMoves(engineBoard, sq);
			long queen_diagonal_moves = rookMoves(engineBoard, sq);

			queens -= 1L << sq;
			moves.addAll(moves(sq, queen_perpendicular_moves, PieceType.QUEEN, engineBoard.toMove));
			moves.addAll(moves(sq, queen_diagonal_moves, PieceType.QUEEN, engineBoard.toMove));
		}
		return moves;
	}

	private static List<AceMove> bishopMoves(ACEBoard engineBoard) {
		List<AceMove> moves = new ArrayList<>();
		long bishops = bishops(engineBoard);
		while(bishops != 0L) {
			int sq = Long.numberOfTrailingZeros(bishops);
			
			long bishop_moves = bishopMoves(engineBoard, sq);
			
			bishops -= 1L << sq;
			moves.addAll(moves(sq, bishop_moves, PieceType.BISHOP, engineBoard.toMove));
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

	private static List<AceMove> rookMoves(ACEBoard engineBoard) {
		List<AceMove> moves = new ArrayList<>();
		long rooks = rooks(engineBoard);
		while(rooks != 0L) {
			int sq = Long.numberOfTrailingZeros(rooks);
			
			long rook_moves = rookMoves(engineBoard, sq);
			
			rooks -= 1L << sq;
			moves.addAll(moves(sq, rook_moves, PieceType.ROOK, engineBoard.toMove));
		}
		return moves;
	}

	private static long rookMoves(ACEBoard engineBoard, int sq) {
		long right_moves = Xray.right_board[sq] & engineBoard.occupied_board;
		right_moves = (right_moves<<1) | (right_moves<<2) | (right_moves<<3) | (right_moves<<4) | (right_moves<<5) | (right_moves<<6); 
		right_moves = right_moves & Xray.right_board[sq];
		right_moves = (right_moves ^ Xray.right_board[sq]) & engineBoard.enemy_and_empty_board;
		long left_moves = Xray.left_board[sq] & engineBoard.occupied_board;
		left_moves = (left_moves>>1) | (left_moves>>2) | (left_moves>>3) | (left_moves>>4) | (left_moves>>5) | (left_moves>>6); 
		left_moves = left_moves & Xray.left_board[sq];
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

	private static List<AceMove> knightMoves(ACEBoard engineBoard) {
		List<AceMove> moves = new ArrayList<>();
		long knights = knights(engineBoard);
		while(knights != 0L) {
			int sq = Long.numberOfTrailingZeros(knights);
			long destinationBitboard = Xray.knight_xray[sq] & engineBoard.enemy_and_empty_board;
			knights -= 1L << sq;
			moves.addAll(moves(sq, destinationBitboard, PieceType.KNIGHT, engineBoard.toMove));
		}
		return moves;
	}

	private static List<AceMove> kingMoves(ACEBoard engineBoard) {
		int sq = Long.numberOfTrailingZeros(kings(engineBoard));
		if(sq == 64) {
			return Collections.emptyList();
		}
		
		long destinationBitboard = Xray.king_xray[sq] & engineBoard.enemy_and_empty_board;
		return moves(sq, destinationBitboard, PieceType.KING, engineBoard.toMove);
	}

	private static List<AceMove> moves(int index, long bitboard, PieceType pieceType, int toMove) {
		List<AceMove> moves = new ArrayList<>();
		Coordinates fromCoordinate = BitboardUtils.coordinates(index);
		
		while(bitboard != 0) {
			int onePos = Long.numberOfTrailingZeros(bitboard);
			Coordinates toCoordinate = BitboardUtils.coordinates(onePos);
			AceMove move = new AceMove(pieceType, toMove, fromCoordinate, toCoordinate, Option.<PieceType> none());
			moves.add(move);
			
			bitboard -= 1L << onePos;
		}
		return moves;
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