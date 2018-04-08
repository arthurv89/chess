package nl.arthurvlug.chess.gui.game.player;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;
import rx.Subscriber;

public class HumanPlayer extends Player {
	@Getter
	private final Set<Subscriber<? super Move>> moveSubscribers = new HashSet<>();

	@Override
	public String getName() {
		return "Human";
	}

	@Override
	public Observable<Move> registerMoveSubscriber() {
		return Observable.create(subscriber -> {
			moveSubscribers.add(subscriber);
		});
	};

	@Override
	public Observable<Void> initialize(final Clock whiteClock, final Clock blackClock) {
		return Observable.empty();
	}

	@Override
	public void notifyNewMove(final ImmutableList<Move> moves) {

	}

	@Override
	public void stop() {
		// Nothing to be stopped
	}
}
