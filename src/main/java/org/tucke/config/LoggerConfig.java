package org.tucke.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.util.CachingDateFormatter;
import org.slf4j.LoggerFactory;

/**
 * @author tucke
 */
public class LoggerConfig {

    private static LoggerConfig instance = new LoggerConfig();
    private final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    public static void load() {
        if (instance == null) {
            synchronized (LoggerConfig.class) {
                instance = new LoggerConfig();
            }
        }
        instance.config();
    }

    private Appender<ILoggingEvent> console() {
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(lc);
        ca.setName("console");
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(lc);
        LayoutBase<ILoggingEvent> layout = new LayoutBase<>() {
            final ThrowableProxyConverter tpc = new ThrowableProxyConverter();

            @Override
            public void start() {
                tpc.start();
                super.start();
            }

            @Override
            public String doLayout(ILoggingEvent event) {
                if (!isStarted()) {
                    return CoreConstants.EMPTY_STRING;
                }
                StringBuilder sb = new StringBuilder();

                long timestamp = event.getTimeStamp();

                sb.append(new CachingDateFormatter("yyyy-MM-dd HH:mm:ss.SSS").format(timestamp));
                sb.append(" [");
                sb.append(event.getThreadName());
                sb.append("] ");
                sb.append(event.getLevel().toString());
                sb.append(" ");
                sb.append(event.getLoggerName());
                sb.append(" - ");
                sb.append(event.getFormattedMessage());
                sb.append(CoreConstants.LINE_SEPARATOR);
                IThrowableProxy tp = event.getThrowableProxy();
                if (tp != null) {
                    String stackTrace = tpc.convert(event);
                    sb.append(stackTrace);
                }
                return sb.toString();
            }
        };
        layout.setContext(lc);
        layout.start();
        encoder.setLayout(layout);
        ca.setEncoder(encoder);
        ca.start();
        return ca;
    }

    private void config() {
        Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.valueOf(AppConfig.getString("logging.level")));
        logger.detachAndStopAllAppenders();
        logger.addAppender(console());
    }

}
