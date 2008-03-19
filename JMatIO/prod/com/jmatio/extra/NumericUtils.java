package com.jmatio.extra;

import static com.jmatio.extra.ArrayUtils.*;

import java.lang.reflect.InvocationTargetException;

import com.jmatio.types.MLNumericArray;

public final class NumericUtils {

	/**
	 * Creates a HD numeric array based on a java array. The input java array
	 * can be any numeric array such as int[3][2] or Double[2][3][4]
	 *
	 * @param <T>
	 * @param clazz
	 *            supported classes are MLDouble, MLInt8, MLInt64, MLUInt8, MLUInt64
	 *            except that MATLAB doesn't support complex integer types.
	 * @param name
	 *            the name of the created array
	 * @param in
	 *            java array either primitive or reference form
	 * @return
	 */
	public static <T extends MLNumericArray<?>> T create(Class<T> clazz,
			String name, Object in) {
		try {
			java.lang.reflect.Constructor<T> constructor = clazz
					.getConstructor(String.class, int[].class);
			int[] dims = size(in);
			T a = constructor.newInstance(name, dims);
			Class<?> base = baseClass(in);
			Object out;
			out = flatten(in);

			if (base.isPrimitive()) {
				out = fromPrimitiveArray(out);
			}

			a.set((Object[]) out);
			return a;

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Reads an element by subscripts
	 *
	 * @param <T>
	 * @param a
	 *            the array
	 * @param sub
	 *            the subscripts
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> T get(M a,
			int... sub) {
		int[] dims = a.getDimensions();
		int idx = sub2idx(dims, sub);
		return a.get(idx);
	}

	/**
	 * Reads the entire MLArray as a java <i>reference</i> array such as
	 * Double[][][] or Integer[][][]
	 *
	 * @param a
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> Object get(M a) {
		return getReal(a);
	}

	/**
	 * Returns the real part of a numeric array as a java <i>reference</i> type
	 * such as Double[3][2][4]
	 * @param <T>
	 * @param <M>
	 * @param a
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number, M extends MLNumericArray<T>> Object getReal(
			M a) {
		int total = a.getSize();
		T[] da = (T[]) createArray(a.getStorageClazz(), total);
		for (int i = 0; i < total; i++) {
			da[i] = a.getReal(i);
		}

		Object out = curl(da, a.getDimensions());
		return out;
	}

	/**
	 * Returns the imaginary part of a numeric array as a java <i>reference</i> type
	 * such as Double[3][2][4]
	 * @param <T>
	 * @param <M>
	 * @param a
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number, M extends MLNumericArray<T>> Object getImaginary(
			M a) {
		int total = a.getSize();
		T[] da = (T[]) createArray(a.getStorageClazz(), total);
		for (int i = 0; i < total; i++) {
			da[i] = a.getImaginary(i);
		}

		Object out = curl(da, a.getDimensions());
		return out;
	}

	/**
	 * Returns the T value given subscripts specified by <i>sub</i>
	 * @param sub
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> T getReal(M a, int[] sub){
		return a.getReal(sub2idx(a.getDimensions(),sub));
	}


	/**
	 * Returns the T value for the imaginary part given subscripts
	 * specified by <i>sub</i>
	 * @param sub
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> T getImaginary(M a, int[] sub) {
		return a.getImaginary(sub2idx(a.getDimensions(), sub));
	}

	/**
	 * Sets the T value for a particular subscript <i>sub</i>
	 * @param value
	 * @param sub
	 */
	public static <T extends Number, M extends MLNumericArray<T>> void set(M a, T value, int[] sub) {
		a.set(value, sub2idx(a.getDimensions(),sub));
	}

	/**
	 * Sets the Double value for a particular subscript <i>sub</i>
	 * @param value
	 * @param sub
	 */
	public static <T extends Number, M extends MLNumericArray<T>> void setReal(M a , T value, int[] sub) {
		a.setReal(value, sub2idx(a.getDimensions(),sub));
	}

	/**
	 * Sets the Double value for a particular subscript <i>sub</i>
	 * @param value
	 * @param sub
	 */
	public static <T extends Number, M extends MLNumericArray<T>> void setImaginary(M a, T value, int[] sub) {
		a.setImaginary(value, sub2idx(a.getDimensions(),sub));
	}

	/**
	 * Sets a HD numeric array based on a java array. The input java array
	 * can be any numeric array such as int[3][2] or Double[2][3][4]
	 *
	 * @param <T>
	 * @param clazz
	 *            the actual class MLDouble, MLInt8, MLInt64, MLUInt8, MLUInt64
	 * @param a
	 *            the name of the created array
	 * @param in
	 *            java array either primitive or reference form
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number, M extends MLNumericArray<T>> void set(M a,
			 Object in) {
		try {
			Class<?> base = baseClass(in);
			Object out;
			out = flatten(in);

			if (base.isPrimitive()) {
				out = fromPrimitiveArray(out);
			}

			a.set((T[]) out);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}




}
