package week04.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * ATM logger
 */
public class AtmLogger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;
    static private FileHandler fileHTML;
    static private AtmHtmlLoggingFormatter formatterHTML;

    static public String ATM_LOGGER = "ATM_LOGGER";
    static Logger atmLogger;

    /**
     * Static Initializer
     */
    static {
        // Configure the root application logger
        atmLogger= Logger.getLogger(ATM_LOGGER);
    }

    /**
     * Returns the singleton instance of the logger
     * @return - Logger
     */
    static Logger getAtmLogger() {
        return atmLogger;
    }

    /**
     * Adds the configured handlers to the provided logger
     * Used by other classes that implemnt logging to
     * ensure the logging is routed to the right files.
     *
     * @param logger
     */
    static public void addAtmHandler(Logger logger) {
        logger.addHandler(fileTxt);
        logger.addHandler(fileHTML);
    }

    /**
     * Initializes the logging system for our purposes
     * @throws IOException
     */
    static public void setup() throws IOException {
        // suppress the loggin output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers.length > 0) {
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        }

        // Set th elog level and file names
        atmLogger.setLevel(Level.INFO);
        fileTxt = new FileHandler("Logging.txt");
        fileHTML = new FileHandler("Logging.html");

        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        atmLogger.addHandler(fileTxt);

        formatterHTML = new AtmHtmlLoggingFormatter();
        fileHTML.setFormatter(formatterHTML);
        atmLogger.addHandler(fileHTML);
    }
}
