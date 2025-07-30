package nl.arthurvlug.chess.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

val slf4j: ReadOnlyProperty<Any, Logger> get() = SLF4JLoggerDelegate()

class SLF4JLoggerDelegate : ReadOnlyProperty<Any, Logger> {
    lateinit var logger: Logger
    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        if (!::logger.isInitialized) logger = LoggerFactory.getLogger(thisRef.javaClass.name)
        return logger
    }
}
