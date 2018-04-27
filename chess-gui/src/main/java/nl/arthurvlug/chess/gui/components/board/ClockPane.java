package nl.arthurvlug.chess.gui.components.board;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import nl.arthurvlug.chess.engine.game.Clock;
import nl.arthurvlug.chess.utils.EventHandler;
import nl.arthurvlug.chess.gui.events.GameFinishedEvent;
import nl.arthurvlug.chess.gui.events.GameStartedEvent;
import nl.arthurvlug.chess.utils.NamedThread;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
@EventHandler
public class ClockPane extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss:SSS");
	
	private Clock whiteClock;
	private Clock blackClock;
	private volatile boolean runThread = true;
	
	public ClockPane() {
		startClockThread();
	}
	
	@Override
	public void paint(Graphics g1) {
		if(whiteClock != null && blackClock != null) {
			Graphics2D g = (Graphics2D) g1;

			drawClock(g, blackClock, 0);
			drawClock(g, whiteClock, 370);
		}
	}

	private void drawClock(Graphics2D g, Clock clock, int yOffset) {
		g.setColor(Color.WHITE);
		g.fillRect(0, yOffset, 100, 30);

		g.setColor(Color.BLACK);
		g.drawRect(0, yOffset, 100, 30);
		g.drawString(clockString(clock), 10, 20 + yOffset);
	}

	private void startClockThread() {
		new NamedThread(new Runnable() {

			public void run() {
				while(runThread) {
					repaint();
				}
			}
		}, "Clock GUI repainter").start();
	}

	private String clockString(Clock clock) {
		return dateTimeFormatter.print(clock.getRemainingTime());
	}

	@Subscribe
	public void on(GameStartedEvent event) {
		this.whiteClock = event.getGame().getWhiteClock();
		this.blackClock = event.getGame().getBlackClock();
		repaint();
	}
	
	@Subscribe
	public void on(GameFinishedEvent event) {
		runThread = false;
	}
}
