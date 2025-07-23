package nl.arthurvlug.chess.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtils {
    public static void logDebug(final Object message) {
        if(MoveUtils.DEBUG) {
            log.info(message.toString());
        }
    }

    private void sysout(final String message) {
        if(MoveUtils.DEBUG) {
            log.info(message);
        }
    }
}
