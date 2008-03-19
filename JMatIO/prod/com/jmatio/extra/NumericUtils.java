package com.jmatio.extra;

import static com.jmatio.extra.ArrayUtils.*;

import java.lang.reflect.InvocationTargetException;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLNumericArray;

/**
 * Utility for handling higher dimensional numeric array. <p>
 * limitation: <br>
 * 1. doesn't support sparse matrix, since some significant change is needed in the MLSparse class <br>
 * 2. don't use any get/set with  complex integer types
 * such as MLInt8+ i*MLInt8 because Matlab doesn't support it
 * @author kdeng
 *
 */
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
	 * @param srcReal
	 *            java array either primitive or reference form
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number, M extends MLNumericArray<T>> M create(Class<M> clazz,
			String name, Object srcReal) {
		try {
			java.lang.reflect.Constructor<M> constructor = clazz
					.getConstructor(String.class, int[].class);
			int[] dims = size(srcReal);
			M a = constructor.newInstance(name, dims);
			Class<?> base = baseClass(srcReal);
			Object out;
			out = flatten(srcReal);

			if (base.isPrimitive()) {
				out = fromPrimitiveArray(out);
			}

			a.set((T[]) out);
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
	 * Creates a HD numeric array based on a java array. The input java array
	 * can be any numeric array such as int[3][2] or Double[2][3][4]
	 *
	 * @param <T>
	 * @param clazz
	 *            supported classes are MLDouble, MLInt8, MLInt64, MLUInt8, MLUInt64
	 *            except that MATLAB doesn't support complex integer types.
	 * @param name
	 *            the name of the created array
	 * @param srcReal
	 *            java array either primitive or reference form
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static  MLDouble createComplex(
			String name, Object srcReal, Object srcImag) {
		try {

			int[] dims = size(srcReal);
			MLDouble a = new MLDouble(name,dims,MLArray.mxDOUBLE_CLASS, MLArray.mtFLAG_COMPLEX);
			Class<?> base = baseClass(srcReal);
			Object out_real;
			Object out_imag;
			out_real = flatten(srcReal);
			out_imag = flatten(srcImag);

			if (base.isPrimitive()) {
				out_real = fromPrimitiveArray(out_real);
				out_imag = fromPrimitiveArray(out_imag);
			}

			a.setReal((Double[]) out_real);
			a.setImaginary((Double[]) out_imag);
			return a;

		} catch (SecurityException e) {
			e.printStackTrace();
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
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
			int[] sub) {
		return getReal(a, sub);
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
	 * Returns the value given subscripts specified by <i>sub</i>
	 * @param sub
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> T getReal(M a, int[] sub){
		return a.getReal(sub2idx(a.getDimensions(),sub));
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
	 * Returns the T value for the imaginary part given subscripts
	 * specified by <i>sub</i>
	 * @param sub
	 * @return
	 */
	public static <T extends Number, M extends MLNumericArray<T>> T getImaginary(M a, int[] sub) {
		return a.getImaginary(sub2idx(a.getDimensions(), sub));
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


	public static <T extends Number, M extends MLNumericArray<T>> void set(M a,
			 Object in) {
		setReal(a,in);
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
	public static <T extends Number, M extends MLNumericArray<T>> void setReal(M a,
			 Object in) {
		try {
			Class<?> base = baseClass(in);
			Object out;
			out = flatten(in);

			if (base.isPrimitive()) {
				out = fromPrimitiveArray(out);
			}

			a.setReal((T[]) out);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

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
	public static <T extends Number, M extends MLNumericArray<T>> void setImaginary(M a,
			 Object in) {
		try {
			Class<?> base = baseClass(in);
			Object out;
			out = flatten(in);

			if (base.isPrimitive()) {
				out = fromPrimitiveArray(out);
			}

			a.setImaginary((T[]) out);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}




}
