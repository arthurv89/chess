package nl.arthurvlug.chess.engine.customEngine;

import java.util.List;

import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.EngineConstants;

public class EngineBoard {
	public int toMove;
	
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

	
	// TODO: Fix for other pieces
//	long whiteOccupiedSquares = white_pawns | white_knights | white_bishops | white_queens | white_kings;
//	long blackOccupiedSquares = black_pawns | black_knights | black_bishops | black_queens | black_kings;
	
	public int minValue;

	public long whiteOccupiedSquares;
	public long blackOccupiedSquares;
	public long enemy_board;
	public long occupied_board;
	public long fullBoard;
	public long enemy_and_empty_board;
	public long empty_board;




	public EngineBoard(int toMove) {
		this();
		this.toMove = toMove;
		finished();
	}

	public EngineBoard(EngineBoard board, int toMove) {
		this(board);
		this.toMove = toMove;
		finished();
	}
	
	public EngineBoard(EngineBoard board) {
		this();
		whiteOccupiedSquares = board.whiteOccupiedSquares;
		blackOccupiedSquares = board.blackOccupiedSquares;
		enemy_board = board.enemy_board;
		occupied_board = board.occupied_board;
		fullBoard = board.fullBoard;
		enemy_and_empty_board = board.enemy_and_empty_board;
		empty_board = board.empty_board;

		toMove = board.toMove;
		
		black_kings = board.black_kings;
		white_kings = board.white_kings;
		black_queens = board.black_queens;
		white_queens = board.white_queens;
		white_rooks = board.white_rooks;
		black_rooks = board.black_rooks;
		white_bishops = board.white_bishops;
		black_bishops = board.black_bishops;
		white_knights = board.white_knights;
		black_knights = board.black_knights;
		white_pawns = board.white_pawns;
		black_pawns = board.black_pawns;
	}

	private EngineBoard() {
	}

	public void apply(List<String> moveList) {
		throw new UnsupportedOperationException();
		
	}

	public EngineBoard move(Move move) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPiece(int engineColor, PieceType pieceType, int fieldIndex) {
		if(PieceType.KING == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_kings |= 1L << fieldIndex;
			} else {
				black_kings |= 1L << fieldIndex;
			}
		}

		if(PieceType.QUEEN == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_queens |= 1L << fieldIndex;
			} else {
				black_queens |= 1L << fieldIndex;
			}
		}

		if(PieceType.ROOK == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_rooks |= 1L << fieldIndex;
			} else {
				black_rooks |= 1L << fieldIndex;
			}
		}

		if(PieceType.BISHOP == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_bishops |= 1L << fieldIndex;
			} else {
				black_bishops |= 1L << fieldIndex;
			}
		}

		if(PieceType.KNIGHT == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_knights |= 1L << fieldIndex;
			} else {
				black_knights |= 1L << fieldIndex;
			}
		}

		if(PieceType.PAWN == pieceType) {
			if(engineColor == EngineConstants.WHITE) {
				white_pawns |= 1L << fieldIndex;
			} else {
				black_pawns |= 1L << fieldIndex;
			}
		}
	}
	
	public void finished() {
		whiteOccupiedSquares = white_pawns | white_knights | white_bishops | white_rooks | white_queens | white_kings;
		blackOccupiedSquares = black_pawns | black_knights | black_bishops | black_rooks | black_queens | black_kings;
		
		enemy_board = toMove == EngineConstants.WHITE
				? blackOccupiedSquares
				: whiteOccupiedSquares;
		occupied_board = whiteOccupiedSquares | blackOccupiedSquares;
		empty_board = fullBoard - occupied_board;
		enemy_and_empty_board = enemy_board | empty_board;
	}

	public boolean inCheck(int toMove) {
		// TODO Auto-generated method stub
		return false;
	}
}
