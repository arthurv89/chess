package nl.arthurvlug.chess.gui.runner;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.gui.ApplicationModule;
import nl.arthurvlug.chess.gui.events.EventHandler;
import nl.arthurvlug.chess.gui.events.ShutdownEvent;
import nl.arthurvlug.chess.gui.events.StartupEvent;
import nl.arthurvlug.chess.utils.NamedThread;

import org.reflections.Reflections;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

@Slf4j
public class Main_Chess {
	private final static Injector injector = Guice.createInjector(new ApplicationModule());

	@Inject
	private EventBus eventBus;

	private void start() throws InterruptedException {
		bindEventHandlers();

		addShutdownHook();

		eventBus.post(new StartupEvent());
		Thread.sleep(Long.MAX_VALUE);
	}

	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(
			new NamedThread(() -> {
				log.info("Shutting down...");
				eventBus.post(new ShutdownEvent());
			}, "Shutdown hook")
		);
	}

	private void bindEventHandlers() {
		Reflections reflections = new Reflections("nl.arthurvlug.chess");
		Set<Class<?>> eventHandlers = reflections.getTypesAnnotatedWith(EventHandler.class);
		for (Class<?> eventHandlerClass : eventHandlers) {
			Object eventHandler = injector.getInstance(eventHandlerClass);
			eventBus.register(eventHandler);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		injector.getInstance(Main_Chess.class).start();
	}
}
