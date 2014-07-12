package nl.arthurvlug.chess;

import java.util.Set;

import nl.arthurvlug.chess.events.EventHandler;
import nl.arthurvlug.chess.events.StartupEvent;

import org.reflections.Reflections;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Main {
	private final static Injector injector = Guice.createInjector(new ApplicationModule());
	
	@Inject
	private EventBus eventBus;


	private void start() {
		bindEventHandlers();
		eventBus.post(new StartupEvent());
	}

	private void bindEventHandlers() {
		Reflections reflections = new Reflections("nl.arthurvlug.chess");
		Set<Class<?>> eventHandlers = reflections.getTypesAnnotatedWith(EventHandler.class);
		for(Class<?> eventHandlerClass : eventHandlers) {
			Object eventHandler = injector.getInstance(eventHandlerClass);
			eventBus.register(eventHandler);
		}
	}

	public static void main(String[] args) {
		injector.getInstance(Main.class).start();
	}
}
