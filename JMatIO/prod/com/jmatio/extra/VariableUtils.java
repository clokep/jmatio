package com.jmatio.extra;

import java.util.regex.Pattern;


/**
 * Utility for ensuring valid variable names.
 * 
 * @author PCLOKE
 */
public class VariableUtils {
	/**
	 * The current (R2011a) default MATLAB <code>namelengthmax</code>.
	 */
	private static int NAME_LENGTH_MAX = 63;
	
	private static Pattern validVariablePattern = Pattern.compile("[a-zA-Z]\\w*");
	
	/**
	 * Check if a variable name is valid (<code>isvarname</code> from MATLAB).
	 * 
	 * Returns true if the {@link String} is a valid MATLAB variable name and false
	 * otherwise. A valid variable name is a character string of letters, digits, and
	 * underscores and beginning with a letter.
	 * 
	 * @param name
	 * @return
	 * @throws MatlabException 
	 */
	public static boolean IsVarName(String name) {
		return VariableUtils.validVariablePattern.matcher(name).matches();
	}
	
	/**
	 * Check if a variable name is valid (<code>isvarname</code> from MATLAB) and unique.
	 * 
	 * Returns true if the {@link String} is a valid MATLAB variable name and false
	 * otherwise. A valid variable name is a character string of letters, digits, and
	 * underscores, totaling not more than <code>namelengthmax</code> characters and
	 * beginning with a letter.
	 * 
	 * Note that variable names are non-unique after <code>namelengthmax</code> characters (currently 63).
	 * 
	 * @param name
	 * @return
	 * @throws MatlabException 
	 */
	public static boolean IsUniqueVarName(String name) {
		return VariableUtils.IsVarName(name) && (name.length() <= VariableUtils.NAME_LENGTH_MAX);
	}
}
