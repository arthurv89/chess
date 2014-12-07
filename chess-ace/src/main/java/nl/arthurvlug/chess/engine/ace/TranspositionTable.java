package nl.arthurvlug.chess.engine.ace;

import java.util.HashMap;
import java.util.Random;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class TranspositionTable {
//	private final HashMap<ColoredPiece, Long> randomNumbers = new HashMap<ColoredPiece, Long>();
//	
//	public TranspositionTable() {
//		final Random random = new Random();
//		for(final PieceType pieceType : PieceType.values()) {
//			final ColoredPiece whitePiece = new ColoredPiece(pieceType, Color.WHITE);
//			randomNumbers.put(whitePiece, random.nextLong());
//			
//			final ColoredPiece blackPiece = new ColoredPiece(pieceType, Color.BLACK);
//			randomNumbers.put(blackPiece, random.nextLong());
//		}
//	}
//	
//	private void put() {
//		
//	}
//	
//	ACEBoard lookForPosition(ACEBoard board) {
//		long hash;
//		for (int i = 0; i < 64; i++) {
//			ColoredPiece piece = board.pieceAt(i);
//			hash
//		}
//		return hash;
//	}
}
