/**
 * Unit-API - Units of Measurement API for Java
 * Copyright (c) 2005-2015, Jean-Marie Dautelle, Werner Keil, V2COM.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
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
package tec.uom.tools.cldr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.measure.format.Parser;
import javax.measure.format.ParserException;

/**
 * @author Werner
 * @version 0.2
 */
final class CLDRParser implements Parser<Object, String> {

	/**
	 * If called stand-alone
	 * 
	 * @param args
	 * @throws Exception
	 * @deprecated remove after test/debugging
	 */
	public static void main(String[] args) throws Exception {
		CLDRParser parsing = new CLDRParser(true);
		parsing.load("/root/units.json");
	}

	private static final Logger logger = Logger.getLogger(CLDRParser.class
			.getName());

	private final boolean verbose;
	private final Set<String> unitSet = new HashSet<>();
	private final Set<String> quantitySet = new HashSet<>();

	CLDRParser(boolean v) {
		this.verbose = v;
	}

	void load(String... files) throws IOException, ParserException {
		if (files != null && files.length > 0) {

			InputStream is = CLDRParser.class.getResourceAsStream(files[0]);
			JsonReader rdr = Json.createReader(is);

			JsonObject obj = rdr.readObject();
			final JsonObject main = obj.getJsonObject("main");
			final JsonObject root = main.getJsonObject("root");
			final JsonObject units = root.getJsonObject("units");
			final JsonObject longUnits = units.getJsonObject("long");
			@SuppressWarnings("rawtypes")
			Set entries = longUnits.entrySet();

			for (Object o : entries) {
				handle(parse(o));
			}
			if (unitSet.size() > 0 && files.length > 1) {
				writeToFile(unitSet, files[1]);
			}
			if (quantitySet.size() > 0 && files.length > 2) {
				writeToFile(quantitySet, files[2]);
			}
		}
	}

	@Override
	public String parse(Object input) throws ParserException {
		return String.valueOf(input);
	}

	private void handle(String st) {
		String[] subStrings = st.split("=");
		if (subStrings != null && subStrings.length > 0) {
			handleUnit(subStrings[0]);
		}
	}

	private void handleUnit(String unitEntry) {
		if (unitEntry.contains("-")) {
			int quantCutOff = unitEntry.indexOf("-");
			String quantity = unitEntry.substring(0, quantCutOff);
			quantitySet.add(quantity);
			String unit = unitEntry.substring(quantCutOff + 1);
			unitSet.add(quantity + ";" + unit);
			if (verbose)
				System.out.println(quantity + ": " + unit);
		} else {
			logger.warning(String.format("'%s' has no quantity, ignoring.",
					unitEntry));
		}
	}

	private void writeToFile(final Set<String> strings, final String fileName) {
		if (fileName != null && fileName.length() > 0) {
			try (FileWriter fw = new FileWriter(fileName);
					BufferedWriter bw = new BufferedWriter(fw)) {
				for (String s : strings) {
					bw.write(s);
					bw.newLine();
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
		}
	}
}
