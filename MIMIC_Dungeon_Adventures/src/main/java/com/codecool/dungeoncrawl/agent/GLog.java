package com.codecool.dungeoncrawl.agent;

import com.codecool.dungeoncrawl.agent.reporter.ReporterClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.codecool.dungeoncrawl.agent.utils.FileHandler.appendToFile;

public class GLog {

    public static final String TAG = "GAME";

    public static final String HIGHLIGHT	= "@@ ";
    public static final String CONNECTION   = "$$ ";
    public static final String AGENT_ERROR  = "!! ";
    public static final String AGENT_COV    = "## ";

    public static final String NEW_LINE	    = "\n";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String OUT_PATH = "./out/DA/out_" + LocalDateTime.now().format(formatter) + "_" + ReporterClient.PREFIX + ".log";
    private static final String COV_PATH = "./cov/DA/cov_" + LocalDateTime.now().format(formatter) + "_" + ReporterClient.PREFIX + ".log";
    private static boolean isFirstLog = true;

    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    public static void i( String text, Object... args ) {

        if (text.startsWith(AGENT_ERROR)) {
            System.out.println( RED + text + RESET );
            ERR_MSG += text.substring(3) + "\n";

        } else if (!text.startsWith(CONNECTION) && !text.startsWith(AGENT_COV)) {
            System.out.println( CYAN + text + RESET );
            BOT_MSG += text.substring(3) + "\n";

        } else if (!text.startsWith(AGENT_COV)) {
            System.out.println( PURPLE + text + RESET );
        }

        if (isFirstLog) {
            appendToFile(OUT_PATH, "======================================================================== " + LocalDateTime.now() + " ========================================================================\n\n");
            appendToFile(COV_PATH, "======================================================================== " + LocalDateTime.now() + " ========================================================================\n\n");
            isFirstLog = false;
        }

        if (text.startsWith(AGENT_COV)) {
            System.out.println( YELLOW + text + RESET );
            appendToFile(COV_PATH, LocalDateTime.now() + "\t" + text);
        }
        else {
            appendToFile(OUT_PATH, LocalDateTime.now() + "\t" + text);
        }
    }

    public static void h( String text, Object... args ) {
        i( HIGHLIGHT + text, args );
    }

    public static void c( String text, Object... args ) {
        i( CONNECTION + text, args );
    }

    public static void e( String text, Object... args ) {
        i( AGENT_ERROR + text, args );
    }

    public static void cov( String text, Object... args ) {
        i( AGENT_COV + text, args );
    }

    public static String BOT_MSG = "";
    public static String ERR_MSG = "";

    public static void resetBotMsg() {
        BOT_MSG = "";
    }

    public static void resetErrMsg() {
        ERR_MSG = "";
    }
}
