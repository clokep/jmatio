package com.jmatio.extra;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class NumericUtilsTest {

	/**
	 * test creating a 3D array,  writing and reading it back
	 * @throws IOException
	 */
	@Test
	public void testCreate() throws IOException {
		// array name
		String name = "doublearr";
		// file name in which array will be storred
		String fileName = "mldouble.mat";



		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= NumericUtils.create(MLDouble.class, name, src3D);
		// write array to file
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(mlDouble);

		// write arrays to file
		new MatFileWriter(fileName, list);

		// read array form file
		MatFileReader mfr = new MatFileReader(fileName);
		MLArray mlArrayRetrived = mfr.getMLArray(name);

		// System.out.println( mlDouble.contentToString() );
		// System.out.println( mlArrayRetrived.contentToString() );
		// test if MLArray objects are equal
		assertEquals("Test if value red from file equals value stored",
				mlDouble, mlArrayRetrived);

		Double[][][] dest3D = (Double[][][]) NumericUtils.get((MLDouble)mlArrayRetrived);
		//System.out.println(ArrayUtils.deepToString(src3D));
		//System.out.println(ArrayUtils.deepToString(dest3D));

		assertTrue(ArrayUtils.equals(src3D, dest3D));


	}

	/**
	 * test read individual elements of a 3D array
	 * @throws IOException
	 */
	@Test
	public void testGetMIntArray() throws IOException {
			// array name
			String name = "doublearr";

			double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
													{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

			Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

			MLDouble mlDouble= NumericUtils.create(MLDouble.class, name, src3D);

			Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

			for (int i=0; i<dest3D.length; i++) {
				for (int j=0; j<dest3D[0].length; j++){
					for (int k=0; k< dest3D[0][0].length; k++){
						dest3D[i][j][k]= NumericUtils.get(mlDouble, i,j,k);
					}
				}
			}

			assertTrue(ArrayUtils.equals(src3D, dest3D));

	}


	@Test
	public void testGetImaginaryM() {
		// array name
		String name = "doublearr";

		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= NumericUtils.createComplex(name, src3D,src3D);

		Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

		for (int i=0; i<dest3D.length; i++) {
			for (int j=0; j<dest3D[0].length; j++){
				for (int k=0; k< dest3D[0][0].length; k++){
					dest3D[i][j][k]= NumericUtils.getImaginary(mlDouble, new int[]{i,j,k});
				}
			}
		}

		assertTrue(ArrayUtils.equals(src3D, dest3D));
	}




	@Test
	public void testSetM() {
		// array name
		String name = "doublearr";

		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= new MLDouble(name, ArrayUtils.size(src3D));

		NumericUtils.set(mlDouble, src3D);

		Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

		for (int i=0; i<dest3D.length; i++) {
			for (int j=0; j<dest3D[0].length; j++){
				for (int k=0; k< dest3D[0][0].length; k++){
					dest3D[i][j][k]= NumericUtils.get(mlDouble, new int[]{i,j,k});
				}
			}
		}

		assertTrue(ArrayUtils.equals(src3D, dest3D));
	}

	@Test
	public void testSetMObjectIntArray() {
		// array name
		String name = "doublearr";

		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= new MLDouble(name, ArrayUtils.size(src3D));

		Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

		for (int i=0; i<dest3D.length; i++) {
			for (int j=0; j<dest3D[0].length; j++){
				for (int k=0; k< dest3D[0][0].length; k++){
					NumericUtils.set(mlDouble, src3D[i][j][k], i,j,k);
					dest3D[i][j][k]= NumericUtils.get(mlDouble, i,j,k);
				}
			}
		}

		assertTrue(ArrayUtils.equals(src3D, dest3D));
	}

	@Test
	public void testSetImaginaryM() {
		// array name
		String name = "doublearr";

		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= new MLDouble(name, ArrayUtils.size(src3D),MLArray.mxDOUBLE_CLASS,MLArray.mtFLAG_COMPLEX);

		Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

		NumericUtils.setImaginary(mlDouble, src3D);
		for (int i=0; i<dest3D.length; i++) {
			for (int j=0; j<dest3D[0].length; j++){
				for (int k=0; k< dest3D[0][0].length; k++){
					//NumericUtils.set(mlDouble, src3D[i][j][k], new int[]{i,j,k});
					dest3D[i][j][k]= NumericUtils.getImaginary(mlDouble, new int[]{i,j,k});
				}
			}
		}

		assertTrue(ArrayUtils.equals(src3D, dest3D));

	}

	@Test
	public void testSetImaginaryMIntArray() {
		// array name
		String name = "doublearr";

		double[][][]  src3D0 = new double[][][]{{{1,2,3,4},{5,6,7,8}, {9,10, 11, 12}},
												{{1,2,3,4}, {5,6,7,8}, {9,10, 11, 12}}};

		Double[][][] src3D = (Double[][][]) ArrayUtils.fromPrimitiveArray(src3D0);

		MLDouble mlDouble= new MLDouble(name, ArrayUtils.size(src3D),MLArray.mxDOUBLE_CLASS,MLArray.mtFLAG_COMPLEX);

		Double[][][] dest3D = (Double[][][]) ArrayUtils.createArray(Double.class, ArrayUtils.size(src3D));

		for (int i=0; i<dest3D.length; i++) {
			for (int j=0; j<dest3D[0].length; j++){
				for (int k=0; k< dest3D[0][0].length; k++){
					NumericUtils.setImaginary(mlDouble, src3D[i][j][k], new int[]{i,j,k});
					dest3D[i][j][k]= NumericUtils.getImaginary(mlDouble, new int[]{i,j,k});
				}
			}
		}

		assertTrue(ArrayUtils.equals(src3D, dest3D));

	}



}
