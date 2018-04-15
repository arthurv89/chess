package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class InitialACEBoard extends ACEBoard {
	public InitialACEBoard() {
		super(initialEngineBoard());
	}

	private static ACEBoard initialEngineBoard() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE, true);
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, FieldUtils.fieldIdx("b1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, FieldUtils.fieldIdx("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, FieldUtils.fieldIdx("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, FieldUtils.fieldIdx("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, FieldUtils.fieldIdx("f1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, FieldUtils.fieldIdx("g1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, FieldUtils.fieldIdx("h1"));
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("a2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("b2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("e2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("f2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("g2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.fieldIdx("h2"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, FieldUtils.fieldIdx("b8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, FieldUtils.fieldIdx("c8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, FieldUtils.fieldIdx("d8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, FieldUtils.fieldIdx("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, FieldUtils.fieldIdx("f8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, FieldUtils.fieldIdx("g8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, FieldUtils.fieldIdx("h8"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("a7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("c7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("d7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("e7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("f7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("g7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.fieldIdx("h7"));
		
		return engineBoard;
	}

}
