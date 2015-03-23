/**
 * Unit-API - Units of Measurement API for Java
 * Copyright (c) 2005-2015, Jean-Marie Dautelle, Werner Keil, V2COM.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-363 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tec.uom.tools.obix;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.measure.Unit;
import javax.tools.Tool;

import tec.uom.lib.common.function.DescriptionSupplier;

/**
 * @author Werner
 * @version 0.2
 */
public class ObixImporter implements Tool {
	
	// TODO factor out, e.g. into uom-lib-common
	static enum ErrorCode {
		OK, Failure
		// For now we'll use ordinal, but should change to code or Id (e.g. using IntIdentifiable)
	}
	
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	/* (non-Javadoc)
	 * @see javax.tools.Tool#run(java.io.InputStream, java.io.OutputStream, java.io.OutputStream, java.lang.String[])
	 */
	@Override
	public int run(InputStream in, OutputStream out, OutputStream err,
			String... arguments) {
		String outPath;
		//Unit kg = ObixUnit.parse("kilogram");
		//System.out.println(kg);		
		List<Unit> units = ObixUnit.units();
		if (units != null && units.size() > 0) {
			for (Unit u : units) {
				logger.fine("Unit: " + u + ", " + u.getName() + ",  " + u.getDimension() + " :: " + ((DescriptionSupplier)u).getDescription());
			}
			
			for (String q : ObixUnit.quantityNames()) {
				logger.fine("Quantity: " + q);
			}
			
			final Map quantities = ObixUnit.quantities();
			for (Object key : quantities.keySet()) {
				logger.fine("Key: " + key + "; Value: " + quantities.get(key));
			}
			
			if (arguments != null && arguments.length > 0) {
				outPath = arguments[0];
				logger.fine("Writing to: " + outPath);
				writeQuantityNames(ObixUnit.quantityNames(), outPath);
			}
			
			return ErrorCode.OK.ordinal();
		} else {
			return ErrorCode.Failure.ordinal();
		}
	}
	
	private void writeQuantityNames(List<String> quantList, String path) {
		final String fileName = "quantities.txt";
		final String fullPath = path + File.separator + fileName;
		logger.fine("Writing to: " + fullPath);
		
	}

	/* (non-Javadoc)
	 * @see javax.tools.Tool#getSourceVersions()
	 */
	@Override
	public Set<SourceVersion> getSourceVersions() {
		return Collections.unmodifiableSet(new HashSet<SourceVersion>(Arrays.asList(
				new SourceVersion[]{SourceVersion.RELEASE_5, SourceVersion.RELEASE_6, 
						SourceVersion.RELEASE_7 }
				)));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Tool importer = new ObixImporter();
		int errorCode = importer.run(System.in, System.out, System.err, args);
		if (errorCode == ErrorCode.OK.ordinal()) {
			System.out.println("Success.");
		} else {
			System.err.println("Error!");
		}
	}
}
