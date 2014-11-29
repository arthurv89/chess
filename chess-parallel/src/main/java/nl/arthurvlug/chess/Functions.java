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

public class Functions {
	private static final AceEvaluator evaluator = new AceEvaluator();
	
	static final DoFn<Position, Position> ADD_NEW_POSITIONS = new DoFn<Position, Position>() {
		private static final long serialVersionUID = 1L;
		public void process(final Position position, final Emitter<Position> emitter) {
			final ACEBoard board = createBoard(position);
			for(AceMove newMove : MoveGenerator.generateMoves(board)) {
				Position childPosition = new Position(newMove.toString(), position);
				emitter.emit(childPosition);
			}
		}
	};

	

	private static ACEBoard createBoard(Position position) {
		final List<String> currentAndAncestorMoves = position.getCurrentAndAncestorMoves();
		
		final ACEBoard board = new InitialEngineBoard();
		board.apply(currentAndAncestorMoves);
		return board;
	}

	public static final CombineFn<String, Position> X = new CombineFn<String, Position>() {
		private static final long serialVersionUID = -412842198L;

		@Override
		public void process(Pair<String, Iterable<Position>> input, Emitter<Pair<String, Position>> emitter) {
			String key = input.first();
			String ss = "";
			for(Position s : input.second()) {
				ss += s.getCurrentAndAncestorsString() + "=" + s.getScore() + " ";
			}
			emitter.emit(new Pair<String, Position>(key, new Position(ss, Position.MIN_POSITION)));
		}
	};

	public static final DoFn<String, Position> ROOT_TO_POSITION = new DoFn<String, Position>() {
		private static final long serialVersionUID = 2670656147763008503L;

		@Override
		public void process(final String __, final Emitter<Position> emitter) {
			emitter.emit(Position.MIN_POSITION);
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
			return parent.getCurrentAndAncestorsString();
		}
	};
	
	@SuppressWarnings("serial")
	static final DoFn<Position, Position> SCORE_POSITIONS = new MapFn<Position, Position>() {
		@Override
		public Position map(final Position position) {
			ACEBoard board = createBoard(position);
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
			Position bestPosition = (better == MIN)
					? Position.MAX_POSITION
					: Position.MIN_POSITION;
			
			for(final Position childPosition : childrenPositions) {
				if(better.apply(new Pair<Position, Position>(childPosition, bestPosition))) {
					bestPosition = new Position(childPosition);
				}
			}
			final Pair<String, Position> moveAndBestPosition = new Pair<String, Position>(move, bestPosition);
			emitter.emit(moveAndBestPosition);
		}

		private Function<Pair<Position, Position>, Boolean> better(Pair<String, Iterable<Position>> twoPositions) {
			return twoPositions.first().length() % 2 == 1 ? MAX : MIN;
		}
	};

	static final Function<Pair<Position, Position>, Boolean> MAX = new Function<Pair<Position, Position>, Boolean>() {
		public Boolean apply(Pair<Position, Position> positions) {
			return positions.first().getScore() > positions.second().getScore();
		}
	};
	static final Function<Pair<Position, Position>, Boolean> MIN = new Function<Pair<Position, Position>, Boolean>() {
		public Boolean apply(Pair<Position, Position> positions) {
			return positions.first().getScore() < positions.second().getScore();
		}
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
