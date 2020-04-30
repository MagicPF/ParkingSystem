package AppKickstarter.misc;

/**
 * A simple class for hosting some useful library functions for
 * our system.
 */
public class Lib {
    //------------------------------------------------------------
    // getTokens
    /**
     * Extract all the tokens from the given string (space delimited).
     * @param str the original string to extract tokens from
     * @return an array of tokens (strings) extracted from str
     */
    public static String [] getTokens(String str) {
	String [] origTokens = str.split("\\s");
	int cnt = 0;
	for (String s : origTokens) {
	    if (s.length() > 0) {
		cnt++;
	    }
	}

	String [] tokens = new String[cnt];
	int j = 0;
	for (String s : origTokens) {
	    if (s.length() > 0) {
		tokens[j++] = s;
	    }
	}
	return tokens;
    } // getTokens
} // Lib
