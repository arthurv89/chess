package nl.arthurvlug.chess.engine.ace.movegeneration;

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
		ImmutableList<AceMove> moves = ImmutableList.<AceMove> builder()
			.addAll(kingMoves(engineBoard))
			.addAll(knightMoves(engineBoard))
			.addAll(rookMoves(engineBoard))
			// TODO: Implement pawn, queen, bishop, castling, en passent
			.build();
		return moves;
	}

	private static List<AceMove> rookMoves(ACEBoard engineBoard) {
		List<AceMove> moves = new ArrayList<>();
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
			
			rooks -= 1L << sq;
			moves.addAll(moves(sq, rook_moves, PieceType.ROOK, engineBoard.toMove));
		}
		return moves;
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