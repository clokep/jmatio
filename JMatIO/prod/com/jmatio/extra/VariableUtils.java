package com.jmatio.extra;

import java.util.regex.Pattern;


/**
 * Utility for ensuring valid variable names.
 * 
 * @author PCLOKE
 */
public class VariableUtils
{
    /**
     * The current (R2011a) default MATLAB <code>namelengthmax</code>.
     */
    private static int NAME_LENGTH_MAX = 63;
    
    /**
     * See "Valid Variable Names" article in MATLAB documentation:
     * "A variable name starts with a letter, followed by any number of letters, digits, or underscores."
     */
    private static Pattern VALID_VARIABLE = Pattern.compile("[a-zA-Z]\\w*");
    
    /**
     * The current (R2011b) default MATLAB keywords, execute <code>iskeyword</code> to generate this list.
     */
    private static String[] MATLAB_KEYWORDS = {"break", "case", "catch", "classdef", "continue",
                                               "else", "elseif", "end", "for", "function", "global",
                                               "if", "otherwise", "parfor", "persistent", "return",
                                               "spmd", "switch", "try", "while"};
    
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
        boolean ret = VariableUtils.VALID_VARIABLE.matcher(name).matches();
        // If it does not match the pattern, return early.
        if (!ret)
        {
            return ret;
        }
  
        // Check that the variable is not in the list of MATLAB keywords.
        for (int i = 0; i < VariableUtils.MATLAB_KEYWORDS.length; i++)
        {
            // If it is a keyword, the compareTo will return 0.
            ret &= VariableUtils.MATLAB_KEYWORDS[i].compareTo(name) != 0;
        }
        return ret;
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
