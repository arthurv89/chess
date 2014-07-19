package nl.arthurvlug.chess.domain.game;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;


public class Clock {
	private DateTime totalClockTimeAvailable;
	private DateTime startTime;
	
	public Clock(int minutes, int seconds) {
		this.totalClockTimeAvailable = new DateTime(DateTimeZone.UTC)
			.withMillis(0)
			.plusMinutes(minutes)
			.plusSeconds(seconds);
	}

	public void stop() {
		totalClockTimeAvailable = getCurrentClock();
		startTime = null;
	}

	public void start() {
		startTime = new DateTime();
	}

	public DateTime getCurrentClock() {
		Period timeTaken = new Period(startTime, new DateTime());
		return totalClockTimeAvailable.minus(timeTaken);
	}
}
