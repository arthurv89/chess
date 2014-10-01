package nl.arthurvlug.chess.engine.ace.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.EngineUtils;
import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.board.pieces.PieceUtils;
import nl.arthurvlug.chess.utils.game.Move;

import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ACEBoard extends AbstractEngineBoard {
	private static MoveGenerator moveGenerator = new MoveGenerator();
	
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
	public boolean lastMoveWasTakeMove;

	// TODO: Implement
	public int fiftyMove = 0;

	// TODO: Implement
	public int repeatedMove = 0;

	@Getter
	@Setter
	private int evaluation;

	public List<ACEBoard> successorBoards;

	public boolean currentPlayerInCheck;




	public ACEBoard(int toMove) {
		this();
		this.toMove = toMove;
		this.evaluation = Integer.MIN_VALUE;
		finalizeBitboards();
	}

	public ACEBoard(ACEBoard board, int toMove) {
		this(board);
		this.toMove = toMove;
		finalizeBitboards();
		generateSuccessorBoards();
	}
	
	public ACEBoard(ACEBoard board) {
		this();
		toMove = board.toMove;
		lastMove = board.lastMove;
		lastMoveWasTakeMove = board.lastMoveWasTakeMove;
		
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
		successorBoards = board.successorBoards;
		finalizeBitboards();
	}

	private ACEBoard() {
	}

	public void apply(List<String> moveList) {
		if(moveList.isEmpty()) {
			return;
		}
		
		Color currentToMove = Color.WHITE;
		for(String sMove : moveList) {
			Move move = MoveUtils.toMove(sMove);
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
			int toMove = EngineConstants.fromColor(movingPiece.getColor());
			apply(new AceMove(movingPiece.getPieceType(), toMove, move.getFrom(), move.getTo(), move.getPromotionPiece()));
		}
//		throw new UnsupportedOperationException();
		
	}

	private ColoredPiece pieceAt(Coordinates from) {
		long bitboard = 1L << BitboardUtils.fieldIdx(from);
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

	public void apply(AceMove move) {
		lastMove = move;
		
		int toIdx = BitboardUtils.fieldIdx(move.getToCoordinate());
		long toBitboard = 1L << toIdx;
		
		lastMoveWasTakeMove = (toBitboard & occupied_board) != 0;
		
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
		int fromIdx = BitboardUtils.fieldIdx(move.getFromCoordinate());
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

		// TODO: Add for a while so we can check for performance issues
//		Preconditions.checkState(successorBoards == null);
	}
	
	public void generateSuccessorBoards() {
		List<ACEBoard> successorBoards = new ArrayList<>();

		ACEBoard opponentMoveBoard = new ACEBoard(this);
		opponentMoveBoard.toMove = EngineUtils.otherToMove(this.toMove);
		opponentMoveBoard.finalizeBitboards();
		MoveGenerator.generateMoves(opponentMoveBoard, false);
		for(ACEBoard board : opponentMoveBoard.successorBoards) {
			if(board.noKings()) {
				currentPlayerInCheck = true;
				break;
			}
		}
		
		
		List<AceMove> moves = MoveGenerator.generateMoves(this);
		for (AceMove move : moves) {
			ACEBoard successorBoard = new ACEBoard(this);
			successorBoard.apply(move);

			successorBoards.add(successorBoard);
		}
		this.successorBoards = successorBoards;
	}

	private boolean noKings() {
		return white_kings == 0L || black_kings == 0L;
	}

	public Integer sideDependentScore(BoardEvaluator evaluator) {
		// TODO: Implement checkmate
		NormalScore score = (NormalScore) evaluator.evaluate(this);
		
		if (toMove == EngineConstants.BLACK) {
			return -score.getValue();
		} else {
			return score.getValue();
		}
	}
	
	@Override
	public String toString() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		Preconditions.checkArgument((whiteOccupiedSquares & blackOccupiedSquares) == 0);
		
		StringBuilder sb = new StringBuilder();
		for (int fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			ColoredPiece pieceAt = pieceAt(BitboardUtils.coordinates(fieldIdx));
			
			if((fieldIdx)%8 == 0) {
				sb.append('\n');
			}
			
			if(pieceAt == null) {
				sb.append('.');
			} else {
				String c = PieceUtils.toCharacterString(pieceAt);
				sb.append(c);
			}
		}
		String reversedBoard = sb.toString();
		
		// Reverse rows
		List<String> l = Lists.newArrayList(Splitter.on('\n').split(reversedBoard));
		Collections.reverse(l);
		return Joiner.on('\n').join(l);
	}

	public void setSuccessorBoards(List<ACEBoard> successorBoards) {
		this.successorBoards = successorBoards;
	}

	public List<ACEBoard> getSuccessorBoards() {
		return successorBoards;
	}
}
