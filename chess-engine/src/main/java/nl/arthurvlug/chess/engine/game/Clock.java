package nl.arthurvlug.chess.engine.game;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;


public class Clock {
	public static final DateTime ZERO_TIME = new DateTime(DateTimeZone.UTC).withMillis(0);
	
	private DateTime totalClockTimeAvailable;
	private DateTime startTime;
	
	public Clock(int minutes, int seconds) {
		this.totalClockTimeAvailable = ZERO_TIME
				.plusMinutes(minutes)
				.plusSeconds(seconds);
	}

	public void stopClock() {
		totalClockTimeAvailable = getRemainingTime();
		startTime = null;
	}

	public void startClock() {
		startTime = new DateTime();
	}

	public DateTime getRemainingTime() {
		Period timeTaken = new Period(startTime, new DateTime());
		DateTime remainingTime = totalClockTimeAvailable.minus(timeTaken);
		
		if(remainingTime.isBefore(Clock.ZERO_TIME)) {
			return Clock.ZERO_TIME;
		}
		
		return remainingTime;
	}

	public boolean isTimeUp() {
		return getRemainingTime() == Clock.ZERO_TIME;
	}
}
