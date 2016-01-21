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


package org.apache.sysml.runtime.matrix.data;

public abstract class SparseBlockFactory
{
	/**
	 * 
	 * @param rlen
	 */
	public static SparseBlock createSparseBlock(int rlen) {
		return createSparseBlock(MatrixBlock.DEFAULT_SPARSEBLOCK, rlen);
	}
	
	/**
	 * 
	 * @param type
	 * @param rlen
	 * @return
	 */
	public static SparseBlock createSparseBlock( SparseBlock.Type type, int rlen ) {
		switch( type ) {
			case MCSR: return new SparseBlockMCSR(rlen, -1);
			case CSR: return new SparseBlockCSR(rlen);
			case COO: return new SparseBlockCOO(rlen);
			default:
				throw new RuntimeException("Unexpected sparse block type: "+type.toString());
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param sblock
	 * @return
	 */
	public static SparseBlock copySparseBlock( SparseBlock.Type type, SparseBlock sblock ) {
		return copySparseBlock(type, sblock, false);
	}
	
	/**
	 * 
	 * @param type
	 * @param sblock
	 * @param forceCopy
	 * @return
	 */
	public static SparseBlock copySparseBlock( SparseBlock.Type type, SparseBlock sblock, boolean forceCopy )
	{
		//check for existing target type
		if( !forceCopy && 
			( (sblock instanceof SparseBlockMCSR && type == SparseBlock.Type.MCSR)
			||(sblock instanceof SparseBlockCSR && type == SparseBlock.Type.CSR)
			||(sblock instanceof SparseBlockCOO && type == SparseBlock.Type.COO))  )
		{
			return sblock;
		}
		
		//create target sparse block
		switch( type ) {
			case MCSR: return new SparseBlockMCSR(sblock);
			case CSR: return new SparseBlockCSR(sblock);
			case COO: return new SparseBlockCOO(sblock);
			default:
				throw new RuntimeException("Unexpected sparse block type: "+type.toString());
		}
	}
}
