package nl.arthurvlug.chess.engine.ace.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
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

public class ACEBoard extends AbstractEngineBoard<ACEBoard> {
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

	long whiteOccupiedSquares;
	long blackOccupiedSquares;
	public long enemy_board;
	public long unoccupied_board;
	public long occupied_board;
	public long enemy_and_empty_board;

	// For castling
	public boolean white_king_or_rook_queen_side_moved;
	public boolean white_king_or_rook_king_side_moved;
	public boolean black_king_or_rook_queen_side_moved;
	public boolean black_king_or_rook_king_side_moved;

	// TODO: Remove
	private Move lastMove;
	// TODO: remove
	boolean lastMoveWasTakeMove;

	// TODO: Implement
	private int fiftyMove = 0;

	// TODO: Implement
	private int repeatedMove = 0;

	@Getter
	@Setter
	private int sideBasedEvaluation;


	public ACEBoard(int toMove, final boolean castlingEnabled) {
		this();
		this.toMove = toMove;
		this.sideBasedEvaluation = Integer.MIN_VALUE;
		if(!castlingEnabled) {
			white_king_or_rook_queen_side_moved = true;
			white_king_or_rook_king_side_moved = true;
			black_king_or_rook_queen_side_moved = true;
			black_king_or_rook_king_side_moved = true;
		}
	}

	public ACEBoard(int toMove) {
		this();
		this.toMove = toMove;
		this.sideBasedEvaluation = Integer.MIN_VALUE;
	}

	public ACEBoard(ACEBoard board, int toMove, final boolean castlingEnabled) {
		this(board);
		this.toMove = toMove;
		finalizeBitboards();
		if(!castlingEnabled) {
			white_king_or_rook_queen_side_moved = true;
			white_king_or_rook_king_side_moved = true;
			black_king_or_rook_queen_side_moved = true;
			black_king_or_rook_king_side_moved = true;
		}
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

		white_king_or_rook_queen_side_moved = board.white_king_or_rook_queen_side_moved;
		white_king_or_rook_king_side_moved = board.white_king_or_rook_king_side_moved;
		black_king_or_rook_queen_side_moved = board.black_king_or_rook_queen_side_moved;
		black_king_or_rook_king_side_moved = board.black_king_or_rook_king_side_moved;
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
			apply(new Move(move.getFrom(), move.getTo(), move.getPromotionPiece()));
		}
//		throw new UnsupportedOperationException();
		
	}

	public ColoredPiece pieceAt(Coordinates from) {
		return pieceAt(BitboardUtils.fieldIdx(from));
	}

	public ColoredPiece pieceAt(String fieldName) {
		return pieceAt(BitboardUtils.toIndex(fieldName));
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

	public void apply(Move move) {
		int toIdx = BitboardUtils.fieldIdx(move.getTo());
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
		int fromIdx = BitboardUtils.fieldIdx(move.getFrom());
		long fromBitboard = 1L << fromIdx;
		long removeFromBoard = ~fromBitboard;

		Preconditions.checkNotNull(pieceAt(fromIdx), String.format("Can't apply move %s: the from field is empty:\n%s", move, this.toString()));
		PieceType movingPiece = pieceAt(fromIdx).getPieceType();
		switch (movingPiece) {
			case PAWN:
				if(toMove == EngineConstants.WHITE) {
					white_pawns &= removeFromBoard;
					white_pawns |= toBitboard;
				} else {
					black_pawns &= removeFromBoard;
					black_pawns |= toBitboard;
				}
				break;
			case BISHOP:
				if(toMove == EngineConstants.WHITE) {
					white_bishops &= removeFromBoard;
					white_bishops |= toBitboard;
				} else {
					black_bishops &= removeFromBoard;
					black_bishops |= toBitboard;
				}
				break;
			case KNIGHT:
				if(toMove == EngineConstants.WHITE) {
					white_knights &= removeFromBoard;
					white_knights |= toBitboard;
				} else {
					black_knights &= removeFromBoard;
					black_knights |= toBitboard;
				}
				break;
			case ROOK:
				if(toMove == EngineConstants.WHITE) {
					if(fromIdx == 0) {
						white_king_or_rook_queen_side_moved = true;
					}
					else if(fromIdx == 7) {
						white_king_or_rook_king_side_moved = true;
					}
					white_rooks &= removeFromBoard;
					white_rooks |= toBitboard;
				} else {
					if(fromIdx == 56) {
						black_king_or_rook_queen_side_moved = true;
					}
					else if(fromIdx == 63) {
						black_king_or_rook_king_side_moved = true;
					}
					black_rooks &= removeFromBoard;
					black_rooks |= toBitboard;
				}
				break;
			case QUEEN:
				if(toMove == EngineConstants.WHITE) {
					white_queens &= removeFromBoard;
					white_queens |= toBitboard;
				} else {
					black_queens &= removeFromBoard;
					black_queens |= toBitboard;
				}
				break;
			case KING:
				if(toMove == EngineConstants.WHITE) {
					white_king_or_rook_queen_side_moved = true;
					white_king_or_rook_king_side_moved = true;
					white_kings &= removeFromBoard;
					white_kings |= toBitboard;
					if(fromIdx == 4 && toIdx == 2) {
						white_rooks &= ~1L;
						white_rooks |= 1L << 3;
					} else if(fromIdx == 4 && toIdx == 6) {
						white_rooks &= ~(1L << 7);
						white_rooks |= 1L << 5;
					}
				} else {
					black_king_or_rook_queen_side_moved = true;
					black_king_or_rook_king_side_moved = true;
					black_kings &= removeFromBoard;
					black_kings |= toBitboard;
					if(fromIdx == 60 && toIdx == 58) {
						black_rooks &= ~(1L << 56);
						black_rooks |= 1L << 59;
					} else if(fromIdx == 60 && toIdx == 62) {
						black_rooks &= ~(1L << 63);
						black_rooks |= 1L << 61;
					}
				}
				break;
			default:
				throw new RuntimeException("Undefined piece");
		}
		this.toMove = ColorUtils.otherToMove(this.toMove);
		this.lastMove = move;
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
		unoccupied_board = ~occupied_board;
		enemy_and_empty_board = enemy_board | unoccupied_board;

		// TODO: Add for a while so we can check for performance issues
//		Preconditions.checkState(successorBoards == null);
	}

	@Override
	public List<ACEBoard> generateSuccessorBoards(final List<Move> generatedMoves) {
		return generatedMoves.stream()
							 .map(move -> createBoardAfterMove(move))
							 .collect(Collectors.toList());
	}

	@Override
	public List<ACEBoard> generateSuccessorTakeBoards() {
		List<Move> moves = AceMoveGenerator.generateMoves(this);

		List<ACEBoard> successorBoards = new ArrayList<>(30);
		for (Move move : moves) {
			int idx = BitboardUtils.fieldIdx(move.getTo());
			if(pieceAt(idx) == null) {
				continue;
			}

			final ACEBoard successorBoard = createBoardAfterMove(move);
			successorBoards.add(successorBoard);
		}
		return successorBoards;
	}

	private ACEBoard createBoardAfterMove(final Move move) {
		ACEBoard successorBoard = new ACEBoard(this);
		successorBoard.finalizeBitboards();
		successorBoard.apply(move);
		return successorBoard;
	}

	@Override
	public String toString() {
		Preconditions.checkArgument((white_pawns & white_knights & white_bishops & white_rooks & white_queens & white_kings) == 0);
		Preconditions.checkArgument((black_pawns & black_knights & black_bishops & black_rooks & black_queens & black_kings) == 0);
		Preconditions.checkArgument((whiteOccupiedSquares & blackOccupiedSquares) == 0, "White and black occupy the same fields. Offending field: \n" + BitboardUtils.toBitboardString((whiteOccupiedSquares & blackOccupiedSquares)));
		
		StringBuilder sb = new StringBuilder();
		for (int fieldIdx = 0; fieldIdx < 64; fieldIdx++) {
			ColoredPiece pieceAt = pieceAt(BitboardUtils.coordinates(fieldIdx));
			
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
		final String s = Joiner.on('\n').join(l);
		return s;
	}

	@Override
	public List<Move> generateMoves() {
		return AceMoveGenerator.generateMoves(this);
	}

	@Override
	public Move getLastMove() {
		return lastMove;
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
	public boolean opponentIsInCheck(final List<Move> generatedMoves) {
		return AceMoveGenerator.opponentIsInCheck(this, generatedMoves);
	}

	@Override
	public int getToMove() {
		return toMove;
	}

	@Override
	public boolean hasNoKing() {
		if(ColorUtils.isWhite(toMove)) {
			return white_kings == 0;
		} else {
			return black_kings == 0;
		}
	}
}
