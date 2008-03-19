package com.jmatio.extra;


import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import static com.jmatio.extra.ArrayUtils.sub2idx;

/**
 * A utility class for manipulating higher dimensional cell arrays
 * @author kdeng
 *
 */
public final class CellUtils {

	/**
	 * Gets a cell element by subscripts
	 * @param c    the cell array
	 * @param sub  the subscripts
	 * @return  the cell (possibly an empty array)
	 */
	public static MLArray get(MLCell c, int... sub){
		int[] dims= c.getDimensions();
		int idx= sub2idx(dims,sub);
		return c.get(idx);
	}

	/**
	 * Sets an element of a cell  by subscripts
	 * @param c     the cell array
	 * @param value the value array
	 * @param sub   the subscripts
	 */
	public static void set(MLCell c, MLArray value, int... sub){
		c.set(value, sub2idx(c.getDimensions(),sub));
	}

}
