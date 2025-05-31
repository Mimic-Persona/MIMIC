/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.shatteredpixel.shatteredpixeldungeon.agent.reporter.ReporterClient;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.shatteredpixel.shatteredpixeldungeon.agent.utils.FileHandler.appendToFile;

public class GLog {

	public static final String TAG = "GAME";
	
	public static final String POSITIVE		= "++ ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String HIGHLIGHT	= "@@ ";

	public static final String CONNECTION   = "$$ ";
	public static final String AGENT_ERROR  = "!! ";
	public static final String AGENT_COV    = "## ";

	public static final String NEW_LINE	    = "\n";

	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	private static final String OUT_PATH = "../../out/SPD/out_" + LocalDateTime.now().format(formatter) + "_" + ReporterClient.PREFIX + ".log";
	private static final String COV_PATH = "../../cov/SPD/cov_" + LocalDateTime.now().format(formatter) + "_" + ReporterClient.PREFIX + ".log";
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
	
	public static Signal<String> update = new Signal<>();

	public static void newLine(){
		update.dispatch( NEW_LINE );
	}
	
	public static void i( String text, Object... args ) {

		if (!text.startsWith(POSITIVE) &&
				!text.startsWith(NEGATIVE) &&
				!text.startsWith(WARNING) &&
				!text.startsWith(HIGHLIGHT) &&
				!text.startsWith(CONNECTION) &&
				!text.startsWith(AGENT_ERROR) &&
				!text.startsWith(AGENT_COV)) {

			BOT_MSG += text + "\n";

		} else if (text.startsWith(AGENT_ERROR)) {
			System.out.println( RED + text + RESET );
			ERR_MSG += text.substring(3) + "\n";

		} else if (!text.startsWith(CONNECTION) && !text.startsWith(AGENT_COV)) {
			BOT_MSG += text.substring(3) + "\n";

		} else if (!text.startsWith(AGENT_COV)) {
			System.out.println( PURPLE + text + RESET );

		} else {
			if (isFirstLog) {
				appendToFile(COV_PATH, "======================================================================== " + LocalDateTime.now() + " ========================================================================\n\n");
				isFirstLog = false;
			}

			System.out.println( YELLOW + text + RESET );
			appendToFile(COV_PATH, LocalDateTime.now() + "\t" + text);

			return;
		}

		if (args.length > 0) {
			text = Messages.format( text, args );
		}

		DeviceCompat.log( TAG, text );
		update.dispatch( text );

		if (isFirstLog) {
			appendToFile(OUT_PATH, "======================================================================== " + LocalDateTime.now() + " ========================================================================\n\n");
			isFirstLog = false;
		}


		appendToFile(OUT_PATH, LocalDateTime.now() + "\t" + text);
	}
	
	public static void p( String text, Object... args ) {
		i( POSITIVE + text, args );
	}
	
	public static void n( String text, Object... args ) {
		i( NEGATIVE + text, args );
	}
	
	public static void w( String text, Object... args ) {
		i( WARNING + text, args );
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
