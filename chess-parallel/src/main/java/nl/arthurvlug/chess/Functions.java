package nl.arthurvlug.chess;

import java.util.List;

import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;

import org.apache.crunch.CombineFn;
import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.MapFn;
import org.apache.crunch.Pair;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class Functions {
	private static final AceEvaluator evaluator = new AceEvaluator();
	
	private static final int MAX_SCORE = 1000;
	
	static final DoFn<Position, Position> ADD_NEW_POSITIONS = new DoFn<Position, Position>() {
		private static final long serialVersionUID = 1L;
		public void process(final Position position, final Emitter<Position> emitter) {
			final String currentAndAncestors = position.getCurrentAndAncestors();
			final List<String> moves = currentAndAncestors.isEmpty()
					? ImmutableList.<String> of()
					: Splitter.on(' ').splitToList(currentAndAncestors);
			
			final ACEBoard board = new InitialEngineBoard();
			board.apply(moves);
			
			final List<AceMove> newMoves = MoveGenerator.generateMoves(board);
			for(AceMove newMove : newMoves) {
				final ACEBoard newBoard = new ACEBoard(board);
				newBoard.apply(newMove);
				emitter.emit(new Position(newMove.toString(), position, newBoard));
			}
		}
	};

	static final DoFn<String, Position> ROOT_TO_POSITION = new DoFn<String, Position>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String _, final Emitter<Position> emitter) {
			emitter.emit(Position.ROOT_MIN);
		}
	};

	static final MapFn<Position, String> ADD_PARENT_AS_KEY = new MapFn<Position, String>() {
		private static final long serialVersionUID = 8871734393164750058L;

		@Override
		public String map(final Position input) {
			final Position parent = input.getParentPosition();
			if(parent == null) {
				return "";
			}
			return parent.getCurrentAndAncestors();
		}
	};
	
	@SuppressWarnings("serial")
	static final DoFn<Position, Position> SCORE_POSITIONS = new MapFn<Position, Position>() {
		@Override
		public Position map(final Position position) {
			ACEBoard board = position.getNewBoard();
			Integer score = evaluator.evaluate(board).getValue();
			position.setScore(score);
			return position;
		}
	};
	static final CombineFn<String, Position> PARENT_TAKE_BEST_MOVE = new CombineFn<String, Position>() {
		private static final long serialVersionUID = -621144572607844850L;

		@Override
		public void process(final Pair<String, Iterable<Position>> moveAndChildrenPositions, final Emitter<Pair<String, Position>> emitter) {
			Iterable<Position> childrenPositions = moveAndChildrenPositions.second();
			String move = moveAndChildrenPositions.first();
			
			Function<Pair<Position, Position>, Boolean> better = better(moveAndChildrenPositions);
			Position bestPosition = better == MIN
					? Position.ROOT_MAX
					: Position.ROOT_MIN;
			
			for(final Position childPosition : childrenPositions) {
				if(better.apply(new Pair<Position, Position>(childPosition, bestPosition))) {
					bestPosition = childPosition;
				}
			}
			final Pair<String, Position> moveAndBestPosition = new Pair<String, Position>(move, bestPosition);
			emitter.emit(moveAndBestPosition);
		}

		private Function<Pair<Position, Position>, Boolean> better(Pair<String, Iterable<Position>> twoPositions) {
			return twoPositions.first().length() % 2 == 1 ? MAX : MIN;
		}

		protected final Function<Pair<Position, Position>, Boolean> MAX = new Function<Pair<Position, Position>, Boolean>() {
			public Boolean apply(Pair<Position, Position> twoPositions) {
				return twoPositions.first().getScore() > twoPositions.second().getScore();
			}
		};
		protected final Function<Pair<Position, Position>, Boolean> MIN = new Function<Pair<Position, Position>, Boolean>() {
			public Boolean apply(Pair<Position, Position> twoPositions) {
				return twoPositions.first().getScore() < twoPositions.second().getScore();
			}
		};
	};
	
	static final DoFn<Position, Position> GET_PARENT = new DoFn<Position, Position>() {
		private static final long serialVersionUID = 5358247606242228527L;

		@Override
		public void process(final Position position, final Emitter<Position> emitter) {
			position.getParentPosition().setScore(position.getScore());
			emitter.emit(position.getParentPosition());
		}
	};
}
