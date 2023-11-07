/*
 * Units of Measurement Tools for Java
 * Copyright (c) 2015-2023, Jean-Marie Dautelle, Werner Keil and others.
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
package tech.uom.tools.cldr;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.help.Help;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.measure.format.MeasurementParseException;
import javax.tools.Tool;

/**
 * @author Werner
 * @version 0.5
 */
public class CLDRImporter implements Tool {
    // TODO factor out, e.g. into uom-tools-common
    static enum ErrorCode {
	OK, Failure
	// For now we'll use ordinal, but should change to code or Id (e.g.
	// using IntIdentifiable)
    }

    private static final Logger logger = Logger.getLogger(CLDRImporter.class.getName());

    static boolean isVerbose;

    // TODO this could be reusable, too, e.g. in uom-tools-common
    static class ToolCommand implements Runnable {
	@Option(type = OptionType.GLOBAL, name = "-v", description = "Verbose mode")
	public boolean verbose;

	public void run() {
	    System.out.println(getClass().getSimpleName());
	}
    }

    @Command(name = "display", description = "Display elements")
    public static final class Display extends ToolCommand {
	@Option(name = "-i", description = "Input file")
	public String inFile;

	@Override
	public void run() {
	    if (inFile != null && inFile.length() > 0) {
		logger.info(getClass().getSimpleName() + " " + inFile);
	    } else {
		logger.info(getClass().getSimpleName());
	    }

	    isVerbose = verbose;

	    // if (verbose) {
	    if (inFile != null && inFile.length() > 0) {
		try {
		    CLDRParser parser = new CLDRParser(true); // for display we
							      // currently
							      // verbose output
		    parser.load(inFile);
		} catch (MeasurementParseException pe) {
		    logger.log(Level.WARNING, getClass().getSimpleName() + " error", pe);
		} catch (IOException ioe) {
		    logger.log(Level.SEVERE, getClass().getSimpleName() + " loading error", ioe);
		}
	    }
	    // }
	}
    }

    @Command(name = "write", description = "Write to file")
    public static final class Write extends ToolCommand {
	@Option(name = "-i", description = "Input file")
	public String inFile;

	@Option(name = "-u", description = "Units output file (CSV)")
	public String unitOutFile;

	@Option(name = "-q", description = "Quantities output file")
	public String quantOutFile;

	@Option(name = "-s", description = "Sort entries")
	public boolean sorted;

	@Arguments(description = "Quantities to write (Filter)")
	public List<String> quantityFilter;

	@Override
	public void run() {
	    final StringBuilder message = new StringBuilder(getClass().getSimpleName());
	    if ((unitOutFile != null && unitOutFile.length() > 0)
		    || (quantOutFile != null && quantOutFile.length() > 0)) {
		message.append(" to ");
		if (unitOutFile != null && unitOutFile.length() > 0) {
		    message.append(unitOutFile);
		    if (quantOutFile != null && quantOutFile.length() > 0) {
			message.append(", ");
			message.append(quantOutFile);
		    }
		} else {
		    if (quantOutFile != null && quantOutFile.length() > 0)
			message.append(quantOutFile);
		}
	    }
	    logger.info(message.toString());
	    isVerbose = verbose;

	    // if (quantities != null) {
	    // for (String q : quantities) {
	    // System.out.println(q);
	    // }
	    // }

	    if ((inFile != null && inFile.length() > 0) && ((unitOutFile != null && unitOutFile.length() > 0)
		    || (quantOutFile != null && quantOutFile.length() > 0))) {
		try {
		    CLDRParser parser = new CLDRParser(verbose);
		    parser.load(inFile, unitOutFile, quantOutFile);
		} catch (MeasurementParseException pe) {
		    logger.log(Level.WARNING, getClass().getSimpleName() + " error", pe);
		} catch (IOException ioe) {
		    logger.log(Level.SEVERE, getClass().getSimpleName() + " loading error", ioe);
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.tools.Tool#run(java.io.InputStream, java.io.OutputStream,
     * java.io.OutputStream, java.lang.String[])
     */
    @Override
    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
	try {
	    @SuppressWarnings("unchecked")
	    CliBuilder<Runnable> builder = Cli.<Runnable>builder(getClass().getSimpleName())
		    .withDescription("Unicode CLDR Importer Tool").withDefaultCommand(Help.class)
		    .withCommands(Help.class, Display.class).withCommands(Help.class, Write.class);
	    Cli<Runnable> toolParser = builder.build();
	    toolParser.parse(arguments).run();

	    return ErrorCode.OK.ordinal();
	} catch (Exception e) {
	    logger.severe(e.getMessage());
	    return ErrorCode.Failure.ordinal();
	}
    }

    @Override
    public final Set<SourceVersion> getSourceVersions() {
	return Collections.unmodifiableSet(new HashSet<SourceVersion>(Arrays.asList(
		new SourceVersion[] { SourceVersion.RELEASE_5, SourceVersion.RELEASE_6, SourceVersion.RELEASE_7 })));
    }

    public static void main(String[] args) throws Exception {
	Tool importer = new CLDRImporter();
	int errorCode = importer.run(System.in, System.out, System.err, args);
	if (errorCode == ErrorCode.OK.ordinal()) {
	    if (isVerbose)
		System.out.println("Success.");
	}
    }
}
