package nl.arthurvlug.chess.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtils {
    public static void logDebug(final Object message, String indent) {
        logDebug(indent + message);
    }

    public static void logDebug(final Object message) {
        log.debug("{}", message);
    }

    private void sysout(final String message) {
        log.info(message);
    }
}
