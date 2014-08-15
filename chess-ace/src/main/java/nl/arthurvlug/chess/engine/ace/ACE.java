package nl.arthurvlug.chess.engine.ace;

import java.util.List;

import com.google.common.collect.ImmutableList;

import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

public class ACE extends CustomEngine {

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	protected Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.apply(moveList);
		return new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator()).think(engineBoard).toMove();
	}
	
	public static void main(String[] args) {
		ImmutableList<String> moves = ImmutableList.<String> of("e2e4", "f7f5");
		
		ACE ace = new ACE();
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
	}
}
