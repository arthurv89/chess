package nl.arthurvlug.chess.engine.ace.board;

public record UnapplyFlags(boolean whiteKingOrRookQueenSideMoved, boolean whiteKingOrRookKingSideMoved,
                           boolean blackKingOrRookQueenSideMoved, boolean blackKingOrRookKingSideMoved,
                           int fiftyMove) {
}
