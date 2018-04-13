package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class InitialEngineBoard extends ACEBoard {
	public InitialEngineBoard() {
		super(initialEngineBoard());
	}

	private static ACEBoard initialEngineBoard() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, FieldUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, FieldUtils.toIndex("b1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, FieldUtils.toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, FieldUtils.toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, FieldUtils.toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, FieldUtils.toIndex("f1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, FieldUtils.toIndex("g1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, FieldUtils.toIndex("h1"));
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("a2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("e2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("f2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("g2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.PAWN, FieldUtils.toIndex("h2"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, FieldUtils.toIndex("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, FieldUtils.toIndex("b8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, FieldUtils.toIndex("c8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, FieldUtils.toIndex("d8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, FieldUtils.toIndex("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, FieldUtils.toIndex("f8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, FieldUtils.toIndex("g8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, FieldUtils.toIndex("h8"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("a7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("c7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("d7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("e7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("f7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("g7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.PAWN, FieldUtils.toIndex("h7"));
		
		return engineBoard;
	}

}
