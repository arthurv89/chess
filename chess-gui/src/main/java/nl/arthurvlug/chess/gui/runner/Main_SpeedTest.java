package nl.arthurvlug.chess.gui.runner;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.ACE;
import org.joda.time.DateTime;

@Slf4j
public class Main_SpeedTest {
	private static class NodeCountMonitor extends Thread {
		private ACE ace;
		private volatile boolean shouldRun = true;

		public NodeCountMonitor(ACE ace) {
			this.ace = ace;
		}
		
		@Override
		public void run() {
			DateTime startTime = DateTime.now();
			while(shouldRun) {
				double secondsPassed = DateTime.now().minus(startTime.getMillis()).getMillis() * 0.001;
				int nodesSearched = ace.getNodesSearched();
				log.debug("{} nodes, {} seconds, {} N/s", nodesSearched, secondsPassed, (int) (nodesSearched / secondsPassed));
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					log.error("Unexpected Exception", e);
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			ACE ace = new ACE(5, "BLACK", new EventBus());

			NodeCountMonitor nodeCountMonitor = new NodeCountMonitor(ace);
			nodeCountMonitor.start();
			ace.startThinking();
			nodeCountMonitor.shouldRun = false;
		} catch(Throwable e) {
			log.error("Unexpected Exception", e);
		}
	}
}
