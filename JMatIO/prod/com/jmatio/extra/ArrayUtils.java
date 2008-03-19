package com.jmatio.extra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

/*
 * Copyright: Thomas McGlynn 1997-1998. This code may be used for any purpose,
 * non-commercial or commercial so long as this copyright notice is retained in
 * the source code or included in or referred to in any derived software.
 */

/** This interface collects some information about Java primitives.
 */
interface PrimitiveInfo {

	/** Suffixes used for the class names for primitive arrays. */
	char[] suffixes = new char[] { 'B', 'S', 'C', 'I', 'J', 'F', 'D', 'Z' };

	/** Classes of the primitives. These should be in widening order
	 * (char is as always a problem).
	 */
	Class<?>[] classes = new Class[] { byte.class, short.class, char.class,
			int.class, long.class, float.class, double.class, boolean.class };

	Class<?>[] wrapped = new Class[] { Byte.class, Short.class,
			Character.class, Integer.class, Long.class, Float.class,
			Double.class, Boolean.class };

	/** Is this a numeric class */
	boolean[] isNumeric = new boolean[] { true, true, true, true, true, true,
			true, false };

	/** Full names */
	String[] types = new String[] { "byte", "short", "char", "int", "long",
			"float", "double", "boolean" };

	/** Sizes */
	int[] sizes = new int[] { 1, 2, 2, 4, 8, 4, 8, 1 };

	/** Index of first element of above arrays referring to a numeric type */
	int FIRST_NUMERIC = 0;

	/** Index of last element of above arrays referring to a numeric type */
	int LAST_NUMERIC = 6;

	int BYTE_INDEX = 0;
	int SHORT_INDEX = 1;
	int CHAR_INDEX = 2;
	int INT_INDEX = 3;
	int LONG_INDEX = 4;
	int FLOAT_INDEX = 5;
	int DOUBLE_INDEX = 6;
	int BOOLEAN_INDEX = 7;
}

/**
 * Utility class to provide array related operations through reflections.
 *
 * Functions specific to primitive arrays usually contain the word "primitive" in their names
 *
 * @author kdeng
 *
 */

public final class ArrayUtils {

	/**
	 * test whether the give object is some kind of array.
	 *
	 * @param o
	 * @return false when o==null or is not an array
	 */
	public static boolean isArray(Object o) {
		if (o == null)
			return false;
		return o.getClass().isArray();
	}

	/**
	 * This routine returns the base class of an object. This is just the class
	 * of the object for non-arrays.
	 */
	public static Class<?> baseClass(Object o) {

		if (o == null) {
			return Void.TYPE;
		}

		String className = o.getClass().getName();

		int dims = 0;
		while (className.charAt(dims) == '[') {
			dims += 1;
		}

		if (dims == 0) {
			return o.getClass();
		}

		char c = className.charAt(dims);
		for (int i = 0; i < PrimitiveInfo.suffixes.length; i += 1) {
			if (c == PrimitiveInfo.suffixes[i]) {
				return PrimitiveInfo.classes[i];
			}
		}

		if (c == 'L') {
			try {
				return Class.forName(className.substring(dims + 1, className
						.length() - 1));
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * This routine returns the class of base array of a multi-dimensional
	 * array. I.e., a one-d array of whatever the array is composed of.
	 * Note that arrays are
	 * not guaranteed to be rectangular, so this returns o[0][0]....
	 */

	public static Object baseArray(Object o) {
		String cname = o.getClass().getName();
		if (cname.charAt(1) == '[') {
			return baseArray(((Object[]) o)[0]);
		} else {
			return o;
		}
	}

	/**
	 * Counts number of elements in an array object. if the input object is not
	 * array, the return will be 1.
	 *
	 * @param o
	 *            any object
	 * @return
	 */
	public static int numel(Object o) {
		String classname = o.getClass().getName();

		//XXX: added classname.length()>=2
		if (classname.length() >= 2 && classname.charAt(1) == '[') {
			int count = 0;
			for (int i = 0; i < ((Object[]) o).length; i += 1) {
				count += numel(((Object[]) o)[i]);
			}
			return count;

		} else if (classname.charAt(0) == '[') {
			return Array.getLength(o);

		} else {
			return 1;
		}
	}

	/**
	 * Finds the sizes of an object.
	 *
	 * This method returns an integer array with the dimensions of the object
	 * <code>o</code> which should usually be an array.
	 *
	 * It returns an array of dimension 0 for scalar objects and it returns -1
	 * for dimension which have not been allocated, e.g., int[][][] x = new
	 * int[100][][]; should return [100,-1,-1].
	 *
	 * @param o
	 *            The object to get the dimensions of.
	 */

	public static int[] size(Object o) {

		if (o == null) {
			return null;
		}

		String classname = o.getClass().getName();

		int ndim = 0;

		while (classname.charAt(ndim) == '[') {
			ndim += 1;
		}

		int[] dimens = new int[ndim];

		for (int i = 0; i < ndim; i += 1) {
			dimens[i] = -1; // So that we can distinguish a null from a 0
			// length.
		}

		for (int i = 0; i < ndim; i += 1) {
			dimens[i] = java.lang.reflect.Array.getLength(o);
			if (dimens[i] == 0) {
				return dimens;
			}
			if (i != ndim - 1) {
				o = ((Object[]) o)[0];
				if (o == null) {
					return dimens;
				}
			}
		}
		return dimens;
	}

	/**
	 * Returns a copy of the object, or null if the object cannot be serialized.
	 * This method may be slow as object input/output streams are used for
	 * copying deeply nested arrays.
	 */
	public static Object copy(Object orig) {
		Object obj = null;
		try {
			// Write the object out to a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();

			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bos.toByteArray()));
			obj = in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		return obj;
	}

	/**
	 * Creates an array with given baseType and dimensions
	 *
	 * @param baseClass
	 *            The base type of the array. This is expected to be a numeric
	 *            type, but this is not checked.
	 * @param dims
	 *            The desired dimensions.
	 * @return An array
	 */

	public static Object createArray(Class<?> baseClass, int[] dims) {

		// Generate an array and populate it with a test pattern of
		// data.

		Object x = Array.newInstance(baseClass, dims);
		if (x == null) {
			throw new OutOfMemoryError("Unable to allocate array: "
					+ baseClass.getName() + deepToString(dims));
		}
		return x;
	}

	/**
	 * Allocate an array dynamically. The Array.newInstance method does not
	 * throw an error.
	 *
	 * @param cl
	 *            The class of the array.
	 * @param length
	 *            The dimension of the array.
	 * @return The allocated array.
	 * @throws An
	 *             OutOfMemoryError if insufficient space is available.
	 */
	public static Object createArray(Class<?> cl, int length) {

		Object o = Array.newInstance(cl, length);
		if (o == null) {
			String desc = cl + "[" + length + "]";
			throw new OutOfMemoryError("Unable to allocate array: " + desc);
		}
		return o;
	}

	/**
	 * Are two objects equal? Arrays have the standard object equals method
	 * which only returns true if the two object are the same. This method
	 * returns true if every element of the arrays match. The inputs may be of
	 * any dimensionality. The dimensionality and dimensions of the arrays must
	 * match as well as any elements. If the elements are non-primitive.
	 * non-array objects, then the equals method is called for each element. If
	 * both elements are multi-dimensional arrays, then the method recurses.
	 */
	public static boolean equals(Object x, Object y) {
		return equals(x, y, 0);
	}

	/**
	 * Are two objects equal? Arrays have the standard object equals method
	 * which only returns true if the two object are the same. This method
	 * returns true if every element of the arrays match. The inputs may be of
	 * any dimensionality. The dimensionality and dimensions of the arrays must
	 * match as well as any elements. If the elements are non-primitive.
	 * non-array objects, then the equals method is called for each element. If
	 * both elements are multi-dimensional arrays, then the method recurses.
	 *
	 * @param x
	 * @param y
	 * @param tol :
	 *            the tolerance in double for primitive arrays,
	 *            <code>(float) tol</code>is used if the inputs are actually
	 *            floating arrays
	 */
	public static boolean equals(Object x, Object y, double tol) {

		// Handle the special cases first.
		// We treat null == null so that two object arrays
		// can match if they have matching null elements.
		if (x == null && y == null) {
			return true;
		}

		if (x == null || y == null) {
			return false;
		}

		Class<?> xClass = x.getClass();
		Class<?> yClass = y.getClass();

		if (xClass != yClass) {
			return false;
		}

		if (!xClass.isArray()) {
			return x.equals(y);

		} else {
			if (xClass.equals(int[].class)) {
				return Arrays.equals((int[]) x, (int[]) y);

			} else if (xClass.equals(double[].class)) {
				if (tol == 0) {
					return Arrays.equals((double[]) x, (double[]) y);
				} else {
					return doubleArrayEquals((double[]) x, (double[]) y, tol);
				}

			} else if (xClass.equals(long[].class)) {
				return Arrays.equals((long[]) x, (long[]) y);

			} else if (xClass.equals(float[].class)) {
				if ((float) tol == 0) {
					return Arrays.equals((float[]) x, (float[]) y);
				} else {
					return floatArrayEquals((float[]) x, (float[]) y,
							(float) tol);
				}

			} else if (xClass.equals(byte[].class)) {
				return Arrays.equals((byte[]) x, (byte[]) y);

			} else if (xClass.equals(short[].class)) {
				return Arrays.equals((short[]) x, (short[]) y);

			} else if (xClass.equals(char[].class)) {
				return Arrays.equals((char[]) x, (char[]) y);

			} else if (xClass.equals(boolean[].class)) {
				return Arrays.equals((boolean[]) x, (boolean[]) y);

			} else {
				// Non-primitive and multidimensional arrays can be
				// cast to Object[]
				Object[] xo = (Object[]) x;
				Object[] yo = (Object[]) y;
				if (xo.length != yo.length) {
					return false;
				}
				for (int i = 0; i < xo.length; i += 1) {
					if (!equals(xo[i], yo[i], tol)) {
						return false;
					}
				}

				return true;

			}
		}
	}

	/** Compare two double arrays using a given tolerance */
	static boolean doubleArrayEquals(double[] x, double[] y, double tol) {

		for (int i = 0; i < x.length; i += 1) {
			//XXX: fixed the following bug:
//			if (x[i] == 0) {
//				return y[i] == 0;
//			}
			if (Math.abs((y[i] - x[i])) > tol * Math.abs(x[i])) {
				return false;
			}
		}
		return true;
	}

	/** Compare two float arrays using a given tolerance */
	static boolean floatArrayEquals(float[] x, float[] y, float tol) {

		for (int i = 0; i < x.length; i += 1) {
			///XXX: fixed the following bug:
//			if (x[i] == 0) {
//				return y[i] == 0;
//			}
			if (Math.abs((y[i] - x[i])) > tol * Math.abs(x[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates an array of a type given by <code>newClass</code> with the
	 * dimensionality given in <i>array</i>.
	 *
	 * @param array
	 *            A possibly multidimensional array to be converted.
	 * @param newClass
	 *            The desired output type. It doesn't have to be the same as
	 *            that of <code> array </code>. That's the whole point of this
	 *            function. However it should usually  one of the class
	 *            descriptors for numeric data, e.g., double.class or
	 *            Double.class.
	 */

	public static Object mimic(Object array, Class<?> newClass) {

		String classname = array.getClass().getName();
		if (classname.charAt(0) != '[') {
			return null;
		}

		int dims = 1;

		while (classname.charAt(dims) == '[') {
			dims += 1;
		}

		Object mimic;

		if (dims > 1) {

			Object[] xarray = (Object[]) array;
			int[] dimens = new int[dims];
			dimens[0] = xarray.length; // Leave other dimensions at 0.

			mimic = createArray(newClass, dimens);

			for (int i = 0; i < xarray.length; i += 1) {
				Object temp = mimic(xarray[i], newClass);
				((Object[]) mimic)[i] = temp;
			}

		} else {
			mimic = createArray(newClass, Array.getLength(array));
		}

		return mimic;
	}

	/**
	 * Generates a description of an array (presumed rectangular).
	 *
	 * @param o
	 *            The array to be described.
	 *
	 * @return A string
	 */

	public static String summary(Object o) {

		Class<?> base = baseClass(o);
		if (base == Void.TYPE) {
			return "NULL";
		}

		int[] dims = size(o);

		StringBuffer desc = new StringBuffer();

		// Note that all instances Class describing a given class are
		// the same so we can use == here.
		boolean found = false;

		for (int i = 0; i < PrimitiveInfo.classes.length; i += 1) {
			if (base == PrimitiveInfo.classes[i]) {
				found = true;
				desc.append(PrimitiveInfo.types[i]);
				break;
			}
		}

		if (!found) {
			desc.append(base.getName());
		}

		if (dims != null) {
			desc.append("[");
			for (int i = 0; i < dims.length; i += 1) {
				desc.append("" + dims[i]);
				if (i < dims.length - 1) {
					desc.append("][");
				}
			}
			desc.append("]");
		}
		return new String(desc);
	}

	/**
	 * @param o
	 *            any object or array
	 * @return String.valueOf(o) if o is null or 1D primitive array; else
	 *         java.util.Arrays.deepToString((Object[])o)
	 *
	 */
	public static String deepToString(Object o) {
		if (!isArray(o)) {
			return String.valueOf(o);
		} else if (isPrimitiveArray(o) && nDims(o) == 1) {
			Class<?> base = baseClass(o);
			if (base == byte.class) {
				return java.util.Arrays.toString((byte[]) o);
			} else if (base == short.class) {
				return java.util.Arrays.toString((short[]) o);
			} else if (base == char.class) {
				return java.util.Arrays.toString((char[]) o);
			} else if (base == int.class) {
				return java.util.Arrays.toString((int[]) o);
			} else if (base == long.class) {
				return java.util.Arrays.toString((long[]) o);
			} else if (base == float.class) {
				return java.util.Arrays.toString((float[]) o);
			} else if (base == double.class) {
				return java.util.Arrays.toString((double[]) o);
			} else
				return java.util.Arrays.toString((boolean[]) o);

		} else
			return java.util.Arrays.deepToString((Object[]) o);
	}

	/**
	 * Returns the number of dimensions of a java object:
	 * <p>
	 * rank(null)==0
	 * <p>
	 * rank(non array) ==1
	 * <p>
	 * rank(array) == the rank of the array
	 * <p>
	 *
	 * @param o
	 * @return
	 */
	public static int nDims(Object o) {
		if (o == null)
			return 0;

		if (!isArray(o))
			return 1;

		String className = o.getClass().getName();

		int dims = 0;
		while (className.charAt(dims) == '[') {
			dims += 1;
		}

		return dims;
	}

	/**
	 * test whether the give object is some kind of primitive array.
	 *
	 * @param o
	 * @return true iff o is a primitive array.
	 */
	public static boolean isPrimitiveArray(Object o) {
		if (!isArray(o)) {
			return false;
		}
		String className = o.getClass().getName();

		int dims = 0;
		while (className.charAt(dims) == '[') {
			dims += 1;
		}

		char c = className.charAt(dims);
		for (int i = 0; i < PrimitiveInfo.suffixes.length; i += 1) {
			if (c == PrimitiveInfo.suffixes[i]) {
				return true;
			}
		}
		return false;
	}

	// /**
	// * Calculate size of the base element of an primitive array.
	// *
	// * @param o
	// * The array object whose base length is desired.
	// * @return the size of the object in bytes, 0 if null, or -1 if not a
	// * primitive array.
	// */
	// public static int sizePrimitive(Object o) {
	//
	// if (o == null) {
	// return 0;
	// }
	//
	// String className = o.getClass().getName();
	//
	// int dims = 0;
	//
	// while (className.charAt(dims) == '[') {
	// dims += 1;
	// }
	//
	// if (dims == 0) {
	// return -1;
	// }
	//
	// char c = className.charAt(dims);
	// for (int i = 0; i < PrimitiveInfo.suffixes.length; i += 1) {
	// if (c == PrimitiveInfo.suffixes[i]) {
	// return PrimitiveInfo.sizes[i];
	// }
	// }
	// return -1;
	// }

	/**
	 *
	 * Copies the contents of an primitive array to its
	 * corresponding object reference version.
	 *
	 * @param in
	 *            the original primitive array such as <i>int[][][]</i>
	 * @return the object version such as <i>Integer[][][]</i>
	 */
	public static Object fromPrimitiveArray(Object in) {
		Class<?> newClass = null;
		if (!isPrimitiveArray(in)) {
			throw new IllegalArgumentException(
					"input argument is not a primitive array");
		}
		;

		Class<?> clazz = baseClass(in);

		for (int i = 0; i < PrimitiveInfo.classes.length; i++) {
			if (clazz.equals(PrimitiveInfo.classes[i])) {
				newClass = PrimitiveInfo.wrapped[i];
				break;
			}
		}

		Object mimic = mimic(in, newClass);
		if (mimic == null) {
			return mimic;
		}

		copyFromPrimitive(in, mimic);

		return mimic;

	}

	/**
	 * Copies a primitive array into an array of its reference type. The
	 * dimensions and dimensionalities of the two arrays should be the same.
	 *
	 * @param array
	 *            The original array such as int[][][]
	 * @param mimic
	 *            The array mimicking the original such as Integer[][][].
	 */
	private static void copyFromPrimitive(Object array, Object mimic) {

		String classname = array.getClass().getName();
		if (classname.charAt(0) != '[') {
			return;
		}

		/* Do multidimensional arrays recursively */
		if (classname.charAt(1) == '[') {

			for (int i = 0; i < ((Object[]) array).length; i += 1) {
				copyFromPrimitive(((Object[]) array)[i], ((Object[]) mimic)[i]);
			}

		} else {

			Byte[] xbarr;
			Short[] xsarr;
			Character[] xcarr;
			Integer[] xiarr;
			Long[] xlarr;
			Float[] xfarr;
			Double[] xdarr;
			Boolean[] xoarr;

			Class<?> base = baseClass(array);
			// Class<?> newType = baseClass(mimic);

			if (base == byte.class) {
				byte[] barr = (byte[]) array;
				xbarr = (Byte[]) mimic;
				for (int i = 0; i < barr.length; i++) {
					xbarr[i] = barr[i];
				}

			} else if (base == short.class) {
				short[] sarr = (short[]) array;
				xsarr = (Short[]) mimic;
				for (int i = 0; i < sarr.length; i += 1)
					xsarr[i] = sarr[i];
			} else if (base == char.class) {
				char[] carr = (char[]) array;
				xcarr = (Character[]) mimic;
				for (int i = 0; i < carr.length; i += 1)
					xcarr[i] = carr[i];
			} else if (base == int.class) {
				int[] iarr = (int[]) array;
				xiarr = (Integer[]) mimic;
				for (int i = 0; i < iarr.length; i += 1)
					xiarr[i] = iarr[i];
			} else if (base == long.class) {
				long[] larr = (long[]) array;
				xlarr = (Long[]) mimic;
				for (int i = 0; i < larr.length; i += 1)
					xlarr[i] = larr[i];
			} else if (base == float.class) {
				float[] farr = (float[]) array;
				xfarr = (Float[]) mimic;
				for (int i = 0; i < farr.length; i += 1)
					xfarr[i] = farr[i];
			} else if (base == double.class) {
				double[] darr = (double[]) array;
				xdarr = (Double[]) mimic;
				for (int i = 0; i < darr.length; i += 1)
					xdarr[i] = darr[i];
			} else if (base == boolean.class) {
				boolean[] dorr = (boolean[]) array;

				xoarr = (Boolean[]) mimic;
				for (int i = 0; i < dorr.length; i += 1)
					xoarr[i] = dorr[i];
			}
		}

		return;

	}

	/**
	 *
	 * Copies the contents of an object wrapped array to its
	 * primitive version
	 *
	 * @param in
	 *            the original primitive array such as <i>Integer[][][]</i>
	 * @return the object version such as <i>int[][][]</i>
	 */
	public static Object toPrimitiveArray(Object in, Object valueForNull) {
		Class<?> newClass = null;
		if (isPrimitiveArray(in)) {
			throw new IllegalArgumentException(
					"input argument is already primitive array");
		}
		;

		Class<?> clazz = baseClass(in);

		for (int i = 0; i < PrimitiveInfo.wrapped.length; i++) {
			if (clazz.equals(PrimitiveInfo.wrapped[i])) {
				newClass = PrimitiveInfo.classes[i];
				break;
			}
		}

		/* First create the full new array. */
		Object mimic = mimic(in, newClass);
		if (mimic == null) {
			return mimic;
		}

		copyToPrimitive(in, mimic, valueForNull);
		return mimic;
	}

	/**
	 * Copies an array into an array of a different type. The dimensions and
	 * dimensionalities of the two arrays should be the same.
	 *
	 * @param array
	 *            The original array.
	 * @param mimic
	 *            The array mimicking the original.
	 */
	private static void copyToPrimitive(Object array, Object mimic,
			Object valueForNull) {

		String classname = array.getClass().getName();
		if (classname.charAt(0) != '[') {
			return;
		}

		/* Do multidimensional arrays recursively */
		if (classname.charAt(1) == '[') {

			for (int i = 0; i < ((Object[]) array).length; i += 1) {
				copyToPrimitive(((Object[]) array)[i], ((Object[]) mimic)[i],
						valueForNull);
			}

		} else {

			Class<?> base = baseClass(array);
			// Class<?> newType = baseClass(mimic);

			if (base == Byte.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Byte[]) array).length; i++) {
						Byte v = ((Byte[]) array)[i];
						((byte[]) mimic)[i] = v == null ? (Byte) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Byte[]) array).length; i++) {
						((Byte[]) mimic)[i] = ((Byte[]) array)[i];
					}
				}

			} else if (base == Short.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Short[]) array).length; i++) {
						Short v = ((Short[]) array)[i];
						((short[]) mimic)[i] = v == null ? (Short) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Short[]) array).length; i++) {
						((short[]) mimic)[i] = ((Short[]) array)[i];
					}
				}
			} else if (base == Character.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Character[]) array).length; i++) {
						Character v = ((Character[]) array)[i];
						((char[]) mimic)[i] = v == null ? (Character) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Character[]) array).length; i++) {
						((char[]) mimic)[i] = ((Character[]) array)[i];
					}
				}
			} else if (base == Integer.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Integer[]) array).length; i++) {
						Integer v = ((Integer[]) array)[i];
						((int[]) mimic)[i] = v == null ? (Integer) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Integer[]) array).length; i++) {
						((int[]) mimic)[i] = ((Integer[]) array)[i];
					}
				}
			} else if (base == Long.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Long[]) array).length; i++) {
						Long v = ((Long[]) array)[i];
						((long[]) mimic)[i] = v == null ? (Long) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Long[]) array).length; i++) {
						((long[]) mimic)[i] = ((Long[]) array)[i];
					}
				}
			} else if (base == Float.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Float[]) array).length; i++) {
						Float v = ((Float[]) array)[i];
						((float[]) mimic)[i] = v == null ? (Float) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Float[]) array).length; i++) {
						((float[]) mimic)[i] = ((Float[]) array)[i];
					}
				}
			} else if (base == Double.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Double[]) array).length; i++) {
						Double v = ((Double[]) array)[i];
						((double[]) mimic)[i] = v == null ? (Double) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Double[]) array).length; i++) {
						((double[]) mimic)[i] = ((Double[]) array)[i];
					}
				}
			} else if (base == Boolean.class) {
				if (valueForNull != null) {
					for (int i = 0; i < ((Boolean[]) array).length; i++) {
						Boolean v = ((Boolean[]) array)[i];
						((boolean[]) mimic)[i] = v == null ? (Boolean) valueForNull
								: v;
					}
				} else {
					for (int i = 0; i < ((Boolean[]) array).length; i++) {
						((boolean[]) mimic)[i] = ((Boolean[]) array)[i];
					}
				}
			}
		}

		return;

	}

	/**
	 * Converts a primitive numeric array to another specified by the base class
	 * <i>newClass</i>
	 *
	 * @param array
	 *                a primtive array
	 * @param newClass
	 *                the primitive class to convert to
	 * @return
	 */
	public static Object betweenPrimitiveArray(Object array, Class<?> newClass) {

		if (!isPrimitiveArray(array)){
			throw new IllegalArgumentException("input array is not primitive");
		}

		/*
		 * We break this up into two steps so that users can reuse an array many
		 * times and only allocate a new array when needed.
		 */


		/* First create the full new array. */
		Object mimic = mimic(array, newClass);
		if (mimic == null) {
			return mimic;
		}

		/* Now copy the info into the new array */
		copyBetweenPrimitive(array, mimic);

		return mimic;
	}

	/**
	 * Copy an array into an array of a different type. The dimensions and
	 * dimensionalities of the two arrays should be the same.
	 *
	 * @param array
	 *            The original array.
	 * @param mimic
	 *            The array mimicking the original.
	 */
	private static void copyBetweenPrimitive(Object array, Object mimic) {

		String classname = array.getClass().getName();
		if (classname.charAt(0) != '[') {
			return;
		}

		/* Do multidimensional arrays recursively */
		if (classname.charAt(1) == '[') {

			for (int i = 0; i < ((Object[]) array).length; i += 1) {
				copyBetweenPrimitive(((Object[]) array)[i],
						((Object[]) mimic)[i]);
			}

		} else {

			byte[] xbarr;
			short[] xsarr;
			char[] xcarr;
			int[] xiarr;
			long[] xlarr;
			float[] xfarr;
			double[] xdarr;

			Class<?> base = baseClass(array);
			Class<?> newType = baseClass(mimic);

			if (base == byte.class) {
				byte[] barr = (byte[]) array;

				if (newType == byte.class) {
					System.arraycopy(array, 0, mimic, 0, barr.length);

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xsarr[i] = barr[i];

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xcarr[i] = (char) barr[i];

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xiarr[i] = barr[i];

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xlarr[i] = barr[i];

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xfarr[i] = barr[i];

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < barr.length; i += 1)
						xdarr[i] = barr[i];
				}

			} else if (base == short.class) {
				short[] sarr = (short[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xbarr[i] = (byte) sarr[i];

				} else if (newType == short.class) {
					System.arraycopy(array, 0, mimic, 0, sarr.length);

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xcarr[i] = (char) sarr[i];

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xiarr[i] = sarr[i];

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xlarr[i] = sarr[i];

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xfarr[i] = sarr[i];

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < sarr.length; i += 1)
						xdarr[i] = sarr[i];
				}

			} else if (base == char.class) {
				char[] carr = (char[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xbarr[i] = (byte) carr[i];

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xsarr[i] = (short) carr[i];

				} else if (newType == char.class) {
					System.arraycopy(array, 0, mimic, 0, carr.length);

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xiarr[i] = carr[i];

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xlarr[i] = carr[i];

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xfarr[i] = carr[i];

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < carr.length; i += 1)
						xdarr[i] = carr[i];
				}

			} else if (base == int.class) {
				int[] iarr = (int[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xbarr[i] = (byte) iarr[i];

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xsarr[i] = (short) iarr[i];

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xcarr[i] = (char) iarr[i];

				} else if (newType == int.class) {
					System.arraycopy(array, 0, mimic, 0, iarr.length);

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xlarr[i] = iarr[i];

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xfarr[i] = iarr[i];

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < iarr.length; i += 1)
						xdarr[i] = iarr[i];
				}

			} else if (base == long.class) {
				long[] larr = (long[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xbarr[i] = (byte) larr[i];

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xsarr[i] = (short) larr[i];

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xcarr[i] = (char) larr[i];

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xiarr[i] = (int) larr[i];

				} else if (newType == long.class) {
					System.arraycopy(array, 0, mimic, 0, larr.length);

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xfarr[i] = (float) larr[i];

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < larr.length; i += 1)
						xdarr[i] = (double) larr[i];
				}

			} else if (base == float.class) {
				float[] farr = (float[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xbarr[i] = (byte) farr[i];

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xsarr[i] = (short) farr[i];

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xcarr[i] = (char) farr[i];

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xiarr[i] = (int) farr[i];

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xlarr[i] = (long) farr[i];

				} else if (newType == float.class) {
					System.arraycopy(array, 0, mimic, 0, farr.length);

				} else if (newType == double.class) {
					xdarr = (double[]) mimic;
					for (int i = 0; i < farr.length; i += 1)
						xdarr[i] = farr[i];
				}

			} else if (base == double.class) {
				double[] darr = (double[]) array;

				if (newType == byte.class) {
					xbarr = (byte[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xbarr[i] = (byte) darr[i];

				} else if (newType == short.class) {
					xsarr = (short[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xsarr[i] = (short) darr[i];

				} else if (newType == char.class) {
					xcarr = (char[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xcarr[i] = (char) darr[i];

				} else if (newType == int.class) {
					xiarr = (int[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xiarr[i] = (int) darr[i];

				} else if (newType == long.class) {
					xlarr = (long[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xlarr[i] = (long) darr[i];

				} else if (newType == float.class) {
					xfarr = (float[]) mimic;
					for (int i = 0; i < darr.length; i += 1)
						xfarr[i] = (float) darr[i];

				} else if (newType == double.class) {
					System.arraycopy(array, 0, mimic, 0, darr.length);
				}
			}
		}

		return;

	}

	/**
	 * Retrieves an element of an array based on its coordinates in the array.
	 * Primitive type is automatically wrapped before it is returned
	 * @param array
	 * @param index the length of <i>index </i> should be == nDims(array)
	 * @return
	 */
	public static Object get(Object array, int[] index) {
		assert nDims(array) == index.length;
		if (index.length == 1) {
			return java.lang.reflect.Array.get(array, index[0]);
		} else {
			Object xarray = ((Object[]) array)[index[0]];
			int[] xindex = new int[index.length - 1];
			System.arraycopy(index, 1, xindex, 0, xindex.length);
			return get(xarray, xindex);
		}
	}

	/**
	 * Sets the value of the indexed component of the specified array object to
	 * the specified new value.
	 * The new value is first automatically unwrapped
	 * if the array has a primitive component type.
	 * @param array
	 * @param index the size of which should be == ndims(array)
	 * @param value
	 */
	public static void set(Object array, int[] index, Object value) {
		assert nDims(array) == index.length;

		if (index.length == 1) {
			java.lang.reflect.Array.set(array, index[0], value);
		} else {
			Object xarray = ((Object[]) array)[index[0]];
			int[] xindex = new int[index.length - 1];
			System.arraycopy(index, 1, xindex, 0, xindex.length);
			set(xarray, xindex, value);
		}
	}

	public static Iterator<int[]> coordIterator(int[] dims) {

		final int depth = dims.length;

		final int[] sizes = dims.clone();

		final int coords[] = new int[depth];

		for (int i = 0; i < coords.length; ++i) {
			coords[i] = 0;
		}

		return new Iterator<int[]>() {
			private boolean done = false;

			public boolean hasNext() {
				return !done;
			}

			public int[] next() {
				int next[] = (int[]) coords.clone();
				advance();
				return next;
			}

			private void advance() {
				int i;
				for (i = 0; i <= coords.length - 1; i++) {
					coords[i]++;
					if (coords[i] >= sizes[i]) {
						coords[i] = 0;
					} else {
						break;
					}
				}
				if (i == coords.length) {
					// We rolled over all axes.
					done = true;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static Object permuteAxes(Object array, int perm[]) {

		int ndims = nDims(array);
		int dims[] = size(array);
		int permuted_coords[] = new int[ndims];
		int permuted_dims[] = new int[ndims];

		permute(perm, permuted_dims, dims);
		Object new_a = createArray(baseClass(array), permuted_dims);

		Iterator<int[]> it = coordIterator(dims);
		while (it.hasNext()) {
			int coords[] = it.next();
			permute(perm, permuted_coords, coords);
			set(new_a, permuted_coords, get(array, coords));
		}
		return new_a;
	}

	static private void permute(int perm[], int dest[], int src[]) {
		for (int i = 0; i < perm.length; ++i) {
			dest[i] = src[perm[i]];
		}
	}

	/**
	 * Reduces a high dimension array to one dimension by following the column-major order
	 * @param array
	 * @return
	 */
	public static Object flatten(Object array){
		int num= numel(array);
		int[] dims= size(array);
		Class<?> baseClass= baseClass(array);
		Object new_a = createArray(baseClass, num);
		Iterator<int[]> it = coordIterator(dims);
		int i=0;
		while (it.hasNext()){
			java.lang.reflect.Array.set(new_a, i++, get(array,it.next()));
		}

		return new_a;
	}

	/**
	 * Curls a flatten one dimensional primitive or reference array into
	 * an array of given dimensions <i>dims</i>
	 * @param array
	 * @return
	 */
	public static Object curl(Object array, int[]dims){
		Class<?> baseClass= baseClass(array);
		Object new_a = createArray(baseClass, dims);
		Iterator<int[]> it = coordIterator(dims);
		int i=0;
		while (it.hasNext()){
			set(new_a, it.next(), java.lang.reflect.Array.get(array, i));
			i++;
		}

		return new_a;
	}

	/**
	 * Returns linear index for high dimensional array from
	 * multiple subscripts
	 *
	 * warning: no argument validation is performed for speed
	 * pre-condition: size.length>=2 && size.length== sub.length
	 *
	 * @param d the size information of an HD array
	 * @param sub  the 0-based multiple scripts (sub1,sub2,...,subn)
	 * @return
	 */
	public static int sub2idx(int[] d, int... sub){
		int[] k= new int[d.length];
		int nidx=sub[0];

		k[0]=1;
		for(int i=1; i<d.length; i++){
			k[i]=k[i-1]*d[i-1];
			nidx+= k[i]*sub[i];

		}

		return nidx;

	}

	/**
	 * Converts a linear index to a subscripts representation
	 * @param d the size information of each dimension
	 * @param index
	 * @return the coordinates (subscripts)
	 */
	public static int[] idx2sub(int[] d, int index){
		int[] idx= new int[d.length];

		idx[0]= index % d[0];
		for (int i=1; i<d.length; i++){
			index= (index -idx[i-1])/d[i-1];
			idx[i]= index % d[i];
		}

		return idx;
	}
}
