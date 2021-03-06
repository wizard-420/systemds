/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.test.functions.privacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.wink.json4j.JSONException;
import org.junit.Test;
import org.apache.sysds.parser.DataExpression;
import org.apache.sysds.runtime.meta.MatrixCharacteristics;
import org.apache.sysds.runtime.privacy.PrivacyConstraint;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;

public class MatrixMultiplicationPropagationTest extends AutomatedTestBase {

	private static final String TEST_DIR = "functions/privacy/";
	private final static String TEST_CLASS_DIR = TEST_DIR + MatrixMultiplicationPropagationTest.class.getSimpleName() + "/";
	private final int m = 20;
	private final int n = 20;
	private final int k = 20;

	@Override
	public void setUp() {
		addTestConfiguration("MatrixMultiplicationPropagationTest",
			new TestConfiguration(TEST_CLASS_DIR, "MatrixMultiplicationPropagationTest", new String[]{"c"}));
	}

	@Test
	public void testMatrixMultiplicationPropagation() throws JSONException {
		matrixMultiplicationPropagation(true, true);
	}

	@Test
	public void testMatrixMultiplicationPropagationFalse() throws JSONException {
		matrixMultiplicationPropagation(false, true);
	}

	@Test
	public void testMatrixMultiplicationPropagationSecondOperand() throws JSONException {
		matrixMultiplicationPropagation(true, false);
	}

	@Test
	public void testMatrixMultiplicationPropagationSecondOperandFalse() throws JSONException {
		matrixMultiplicationPropagation(false, false);
	}

	private void matrixMultiplicationPropagation(boolean privacy, boolean privateFirstOperand) throws JSONException {

		TestConfiguration config = availableTestConfigurations.get("MatrixMultiplicationPropagationTest");
		loadTestConfiguration(config);
		fullDMLScriptName = SCRIPT_DIR + TEST_DIR + config.getTestScript() + ".dml";
		programArgs = new String[]{"-nvargs",
			"a=" + input("a"), "b=" + input("b"), "c=" + output("c"),
			"m=" + m, "n=" + n, "k=" + k};

		double[][] a = getRandomMatrix(m, n, -1, 1, 1, -1);
		double[][] b = getRandomMatrix(n, k, -1, 1, 1, -1);
		double[][] c = TestUtils.performMatrixMultiplication(a, b);
		
		PrivacyConstraint privacyConstraint = new PrivacyConstraint(privacy);
		MatrixCharacteristics dataCharacteristics = new MatrixCharacteristics(m,n,k,k);

		if ( privateFirstOperand ) {
			writeInputMatrixWithMTD("a", a, false, dataCharacteristics, privacyConstraint);
			writeInputMatrix("b", b);
		}
		else {
			writeInputMatrix("a", a);
			writeInputMatrixWithMTD("b", b, false, dataCharacteristics, privacyConstraint);
		}
		
		writeExpectedMatrix("c", c);

		runTest(true,false,null,-1);

		// Check that the output data is correct
		compareResults(1e-9);

		// Check that the output metadata is correct
		String actualPrivacyValue = readDMLMetaDataValue("c", OUTPUT_DIR, DataExpression.PRIVACY);
		assertEquals(String.valueOf(privacy), actualPrivacyValue);
	}

	@Test
	public void testMatrixMultiplicationNoPropagation() {
		matrixMultiplicationNoPropagation();
	}

	private void matrixMultiplicationNoPropagation() {
		TestConfiguration config = availableTestConfigurations.get("MatrixMultiplicationPropagationTest");
		loadTestConfiguration(config);
		fullDMLScriptName = SCRIPT_DIR + TEST_DIR + config.getTestScript() + ".dml";
		programArgs = new String[]{ "-nvargs", 
			"a=" + input("a"), "b=" + input("b"), "c=" + output("c"),
			"m=" + m, "n=" + n, "k=" + k};

		double[][] a = getRandomMatrix(m, n, -1, 1, 1, -1);
		double[][] b = getRandomMatrix(n, k, -1, 1, 1, -1);
		double[][] c = TestUtils.performMatrixMultiplication(a, b);
		
		
		writeInputMatrix("a", a);
		writeInputMatrix("b", b);
		writeExpectedMatrix("c", c);

		runTest(true,false,null,-1);

		// Check that the output data is correct
		compareResults(1e-9);

		// Check that a JSONException is thrown 
		// because no privacy metadata should be written to c
		boolean JSONExceptionThrown = false;
		try{
			readDMLMetaDataValue("c", OUTPUT_DIR, DataExpression.PRIVACY);
		} catch (JSONException e){
			JSONExceptionThrown = true;
		} catch (Exception e){
			fail("Exception occured, but JSONException was expected. The exception thrown is: " + e.getMessage());
			e.printStackTrace();
		}
		assert(JSONExceptionThrown);
	}

	@Test
	public void testMatrixMultiplicationPrivacyInputTrue() throws JSONException {
		testMatrixMultiplicationPrivacyInput(true);
	}

	@Test
	public void testMatrixMultiplicationPrivacyInputFalse() throws JSONException {
		testMatrixMultiplicationPrivacyInput(false);
	}

	private void testMatrixMultiplicationPrivacyInput(boolean privacy) throws JSONException {
		TestConfiguration config = availableTestConfigurations.get("MatrixMultiplicationPropagationTest");
		loadTestConfiguration(config);

		double[][] a = getRandomMatrix(m, n, -1, 1, 1, -1);
		
		PrivacyConstraint privacyConstraint = new PrivacyConstraint();
		privacyConstraint.setPrivacy(privacy);
		MatrixCharacteristics dataCharacteristics = new MatrixCharacteristics(m,n,k,k);
		
		writeInputMatrixWithMTD("a", a, false, dataCharacteristics, privacyConstraint);

		String actualPrivacyValue = readDMLMetaDataValue("a", INPUT_DIR, DataExpression.PRIVACY);
		assertEquals(String.valueOf(privacy), actualPrivacyValue);
	}
}
