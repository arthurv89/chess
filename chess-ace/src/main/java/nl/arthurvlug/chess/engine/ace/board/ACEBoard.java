package nl.arthurvlug.chess.engine.ace.board;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.EngineUtils;
import nl.arthurvlug.chess.engine.ace.alphabeta.AceMove;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import com.google.common.base.Preconditions;

public class ACEBoard extends AbstractEngineBoard {
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
	
	public final long fullBoard = -1;
	public long whiteOccupiedSquares;
	public long blackOccupiedSquares;
	public long empty_board;
	public long enemy_board;
	public long occupied_board;
	public long enemy_and_empty_board;

	public AceMove lastMove;

	// TODO: Implement
	public int fiftyMove = 0;

	// TODO: Implement
	public int repeatedMove = 0;

	@Getter
	@Setter
	private int evaluation;




	public ACEBoard(int toMove) {
		this();
		this.toMove = toMove;
		finalizeBitboards();
	}

	public ACEBoard(ACEBoard board, int toMove) {
		this(board);
		this.toMove = toMove;
		finalizeBitboards();
	}
	
	public ACEBoard(ACEBoard board) {
		this();
		toMove = board.toMove;
		lastMove = board.lastMove;
		
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
		finalizeBitboards();
	}

	private ACEBoard() {
	}

	public void apply(List<String> moveList) {
		throw new UnsupportedOperationException();
		
	}

	public void apply(AceMove move) {
		lastMove = move;
		
		int toIdx = BitboardUtils.toIndex(move.getToCoordinate());
		long toBitboard = 1L << toIdx;
		
		// Remove pieces from destination field
		long removeToBoard = ~toBitboard;
		black_kings &= removeToBoard;
		white_kings &= removeToBoard;
		black_queens &= removeToBoard;
		white_queens &= removeToBoard;
		white_rooks &= removeToBoard;
		black_rooks &= removeToBoard;
		white_bishops &= removeToBoard;
		black_bishops &= removeToBoard;
		white_knights &= removeToBoard;
		black_knights &= removeToBoard;
		white_pawns &= removeToBoard;
		black_pawns &= removeToBoard;
		
		// toBitboard
		int fromIdx = BitboardUtils.toIndex(move.getFromCoordinate());
		long fromBitboard = 1L << fromIdx;
		long removeFromBoard = ~fromBitboard;
		
		switch (move.getMovingPiece()) {
			case PAWN:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_pawns &= removeFromBoard;
					white_pawns |= toBitboard;
				} else {
					black_pawns &= removeFromBoard;
					black_pawns |= toBitboard;
				}
				break;
			case BISHOP:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_bishops &= removeFromBoard;
					white_bishops |= toBitboard;
				} else {
					black_bishops &= removeFromBoard;
					black_bishops |= toBitboard;
				}
				break;
			case KNIGHT:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_knights &= removeFromBoard;
					white_knights |= toBitboard;
				} else {
					black_knights &= removeFromBoard;
					black_knights |= toBitboard;
				}
				break;
			case ROOK:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_rooks &= removeFromBoard;
					white_rooks |= toBitboard;
				} else {
					black_rooks &= removeFromBoard;
					black_rooks |= toBitboard;
				}
				break;
			case QUEEN:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_queens &= removeFromBoard;
					white_queens |= toBitboard;
				} else {
					black_queens &= removeFromBoard;
					black_queens |= toBitboard;
				}
				break;
			case KING:
				if(move.getToMove() == EngineConstants.WHITE) {
					white_kings &= removeFromBoard;
					white_kings |= toBitboard;
				} else {
					black_kings &= removeFromBoard;
					black_kings |= toBitboard;
				}
				break;
			default:
				throw new RuntimeException("Undefined piece");
		}
		toMove = EngineUtils.otherToMove(toMove);
		finalizeBitboards();
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
	
	public void finalizeBitboards() {
		whiteOccupiedSquares = white_pawns | white_knights | white_bishops | white_rooks | white_queens | white_kings;
		blackOccupiedSquares = black_pawns | black_knights | black_bishops | black_rooks | black_queens | black_kings;
		
		enemy_board = toMove == EngineConstants.WHITE
				? blackOccupiedSquares
				: whiteOccupiedSquares;
		occupied_board = whiteOccupiedSquares | blackOccupiedSquares;
		empty_board = ~occupied_board;
		enemy_and_empty_board = enemy_board | empty_board;
	}

	public boolean inCheck(int toMove) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		
		return BitboardUtils.toBitboardString(occupied_board);
	}
}
