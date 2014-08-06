package nl.arthurvlug.chess.engine.ace;

import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;

public class InitialEngineBoard extends ACEBoard {
	public InitialEngineBoard() {
		super(initialEngineBoard());
	}

	private static ACEBoard initialEngineBoard() {
		ACEBoard engineBoard = new ACEBoard(EngineConstants.WHITE);
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("a1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("b1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("c1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, BitboardUtils.toIndex("d1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("e1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("f1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("g1"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("h1"));
		
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("a2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("b2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("c2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.QUEEN, BitboardUtils.toIndex("d2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KING, BitboardUtils.toIndex("e2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.BISHOP, BitboardUtils.toIndex("f2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.KNIGHT, BitboardUtils.toIndex("g2"));
		engineBoard.addPiece(EngineConstants.WHITE, PieceType.ROOK, BitboardUtils.toIndex("h2"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("a8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("b8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("c8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, BitboardUtils.toIndex("d8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("e8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("f8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("g8"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("h8"));
		
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("a7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("b7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("c7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.QUEEN, BitboardUtils.toIndex("d7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KING, BitboardUtils.toIndex("e7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.BISHOP, BitboardUtils.toIndex("f7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.KNIGHT, BitboardUtils.toIndex("g7"));
		engineBoard.addPiece(EngineConstants.BLACK, PieceType.ROOK, BitboardUtils.toIndex("h7"));
		
		return engineBoard;
	}

}
