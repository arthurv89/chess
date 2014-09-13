package nl.arthurvlug.chess.engine.ace;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

public class ACE extends CustomEngine {
	int DEPTH = 2;

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	protected Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.apply(moveList);
		AceMove aceMove = new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator()).think(engineBoard, DEPTH);
		return aceMove.toMove();
	}
	
	public static void main(String[] args) {
//		ImmutableList<String> moves = ImmutableList.<String> of("g1h3", "a7a6");
		List<String> moves = Splitter.on(' ').splitToList("b1a3 b8c6 g1f3 g8f6 a3b1 e7e5");
		
		ACE ace = new ACE();
		Move move = ace.think(moves, new ThinkingParams());
		System.out.println(move);
	}
}
