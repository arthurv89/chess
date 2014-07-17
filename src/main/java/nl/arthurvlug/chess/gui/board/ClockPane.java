package nl.arthurvlug.chess.gui.board;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import nl.arthurvlug.chess.domain.game.Clock;
import nl.arthurvlug.chess.domain.game.GameStartedEvent;
import nl.arthurvlug.chess.events.EventHandler;

import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
@EventHandler
public class ClockPane extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss:SSS");
	
	private Clock whiteClock;
	private Clock blackClock;
	
	public ClockPane() {
		startClockThread();
	}
	
	@Override
	public void paint(Graphics g1) {
		if(whiteClock != null && blackClock != null) {
			Graphics2D g = (Graphics2D) g1;

			drawBlackClock(g);
			drawWhiteClock(g);
		}
	}

	private void drawBlackClock(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 100, 30);

		g.setColor(Color.BLACK);
		g.drawRect(0, 0, 100, 30);
		g.drawString(clockString(blackClock), 10, 20);
	}

	private void drawWhiteClock(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 370, 100, 30);

		g.setColor(Color.BLACK);
		g.drawRect(0, 370, 100, 30);
		g.drawString(clockString(whiteClock), 10, 390);
	}

	private void startClockThread() {
		new Thread(new Runnable() {
			public void run() {
				while(true) {
					repaint();
				}
			}
		}).start();
	}

	private String clockString(Clock clock) {
//		long totalMillis = clock.getCurrentClock().getMillis();
//		long minutes = totalMillis / 60000;
//		long seconds = totalMillis / 1000 - minutes * 60;
//		long millis = totalMillis % 1000;
		
		return dateTimeFormatter.print(clock.getCurrentClock());
//		return minutes + " : " + seconds + " : " + millis/10;
	}

	@Subscribe
	public void on(GameStartedEvent event) {
		this.whiteClock = event.getGame().getWhiteClock();
		this.blackClock = event.getGame().getBlackClock();
		repaint();
	}
}
