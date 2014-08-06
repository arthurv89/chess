package nl.arthurvlug.chess.engine.movegeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;

import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableList;

public class MoveGenerator {
	private final static long empty_board = 0L;

	
	public static List<Move> generateMoves(ACEBoard engineBoard) {
		return ImmutableList.<Move> builder()
			.addAll(kingMoves(engineBoard))
			.addAll(knightMoves(engineBoard))
			.addAll(rookMoves(engineBoard))
			.build();
	}

	private static List<Move> rookMoves(ACEBoard engineBoard) {
		List<Move> moves = new ArrayList<>();
		long rooks = rooks(engineBoard);
		while(rooks != 0L) {
			int sq = Long.numberOfTrailingZeros(rooks);
			
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
			long rook_captures = rook_moves & engineBoard.enemy_board;
			long rook_non_captures = rook_moves & empty_board;
			
			long destinationBitboard = rook_moves | rook_captures | rook_non_captures;

			rooks -= 1L << sq;
			moves.addAll(moves(sq, destinationBitboard));
		}
		return moves;
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

	private static List<Move> moves(int index, long bitboard) {
		List<Integer> ones = findOnes(bitboard);
		
		List<Move> moves = new ArrayList<>();
		Coordinates fromCoordinate = BitboardUtils.coordinates(index);
		for(Integer onePos : ones) {
			Coordinates toCoordinate = BitboardUtils.coordinates(onePos);
			Move move = new Move(fromCoordinate, toCoordinate, Option.<PieceType> none());
			moves.add(move);
		}
		return moves;
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

	private static List<Integer> findOnes(long bitboard) {
		int sq=0;
		List<Integer> ones = new ArrayList<>();
		while(bitboard != 0) {
			if(bitboard % 2 == 1) {
				ones.add(sq);
			}
			bitboard = bitboard >> 1;
			sq++;
		}
		return ones;
	}
}
