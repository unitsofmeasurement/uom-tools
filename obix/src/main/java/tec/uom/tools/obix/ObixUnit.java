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

//
//Portions: 20 Dec 08  Brian Frank (Fantom)
//

import static tec.uom.tools.obix.LocalHelpers.dup;
import static tec.uom.tools.obix.LocalHelpers.split;

import javax.measure.Dimension;
import javax.measure.IncommensurableException;
import javax.measure.MeasurementException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.format.ParserException;

import tec.uom.lib.common.function.DescriptionSupplier;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * oBIX representation of a Unit
 * @version 0.2
 * @author Werner Keil
 */
@SuppressWarnings("rawtypes")
final class ObixUnit implements Unit, DescriptionSupplier

{
	private static final String DEFAULT_PATH = "src/main/java/tec/uom/tools/obix/units.txt";

	// ////////////////////////////////////////////////////////////////////////
	// Database
	// ////////////////////////////////////////////////////////////////////////

	public static ObixUnit parse(String name) {
		return parse(name, true);
	}

	public static ObixUnit parse(String name, boolean checked) {
		synchronized (byId) {
			ObixUnit unit = (ObixUnit) byId.get(name);
			if (unit != null || !checked)
				return unit;
			throw new MeasurementException("Unit not found: " + name);
		}
	}

	public static List<Unit> units() {
		synchronized (list) {
			return Collections.unmodifiableList(dup(list.toArray(), list.size()));
		}
	}

	public static List<String> quantityNames() {
		return quantityNames;
	}

	public static Map quantities() {
		return quantities;
	}
	
	public static List quantity(String quantity) {
		List list = (List) quantities.get(quantity);
		if (list == null)
			throw new MeasurementException("Unknown unit database quantity: " + quantity);
		return list;
	}

	private static List<String> loadDatabase() {
		LineNumberReader in = null;
		List<String> quantityNames = new ArrayList<>();
		try {
			// open etc/sys/units.txt
			//String path = "etc/sys/units.txt";
			String path = DEFAULT_PATH;
			/*
			if (Sys.isJarDist)
				in = new FileInputStream(ObixUnit.class.getClassLoader()
						.getResourceAsStream(path));
			else
				in = Env.cur().findFile(Uri.fromStr(path)).in();
*/
			FileReader fr = new FileReader(path);
		    in = new LineNumberReader(fr);
			//in =  new LineNumberReader(new InputStreamReader(ObixUnit.class.getResourceAsStream(path)));
			
			// parse each line
			String curQuantityName = null;
			List curQuantityList = null;
			String line;
			while ((line = in.readLine()) != null) {
				// skip comment and blank lines
				line = line.trim();
				if (line.startsWith("//") || line.length() == 0)
					continue;

				// quantity sections delimited as "-- name (dim)"
				if (line.startsWith("--")) {
					if (curQuantityName != null)
						quantities.put(curQuantityName,
								Collections.unmodifiableList(curQuantityList));
					curQuantityName = line.substring(2, line.indexOf('('))
							.trim();
					curQuantityList = new ArrayList<ObixUnit>();
					quantityNames.add(curQuantityName);
					continue;
				}

				// must be a unit
				try {
					ObixUnit unit = ObixUnit.define(line);
					curQuantityList.add(unit);
				} catch (Exception e) {
					System.err
							.println("WARNING: Init unit in " + DEFAULT_PATH + ": "
									+ line);
					System.err.println("  " + e);
				}
			}
			quantities.put(curQuantityName, Collections.unmodifiableList(curQuantityList));
		} catch (Throwable e) {
			try {
				in.close();
			} catch (Exception e2) {
			}
			System.out.println("WARNING: Cannot load " + DEFAULT_PATH);
			e.printStackTrace();
		}
		return (List) Collections.unmodifiableList(quantityNames);
	}

	// ////////////////////////////////////////////////////////////////////////
	// Definition
	// ////////////////////////////////////////////////////////////////////////

	public static ObixUnit define(String str) {
		// parse
		ObixUnit unit = null;
		try {
			unit = parseUnit(str);
		} catch (Throwable e) {
			String msg = str;
			if (e instanceof ParserException)
				msg += ": " + ((ParserException) e).getMessage();
			throw new MeasurementException("Unit", e);
		}

		// register
		synchronized (byId) {
			// check that none of the units are defined
			for (int i = 0; i < unit.ids.size(); ++i) {
				String id = (String) unit.ids.get(i);
				if (byId.get(id) != null)
					throw new MeasurementException("Unit id already defined: " + id);
			}

			// this is a new definition
			for (int i = 0; i < unit.ids.size(); ++i) {
				String id = (String) unit.ids.get(i);
				byId.put(id, unit);
			}
			list.add(unit);
		}

		return unit;
	}

	/**
	 * Parse an un-interned unit: unit := <ids> [";" <dim> [";" <scale> [";"
	 * <offset>]]]
	 */
	private static ObixUnit parseUnit(String s) {
		String idStrs = s;
		int c = s.indexOf(';');
		if (c > 0)
			idStrs = s.substring(0, c);
		List ids = split(idStrs, Long.valueOf(','));
		if (c < 0)
			return new ObixUnit(ids, dimensionless, 1, 0);

		String dim = s = s.substring(c + 1).trim();
		c = s.indexOf(';');
		if (c < 0)
			return new ObixUnit(ids, parseDim(dim), 1, 0);

		dim = s.substring(0, c).trim();
		String scale = s = s.substring(c + 1).trim();
		c = s.indexOf(';');
		if (c < 0)
			return new ObixUnit(ids, parseDim(dim), Double.parseDouble(scale), 0);

		scale = s.substring(0, c).trim();
		String offset = s.substring(c + 1).trim();
		return new ObixUnit(ids, parseDim(dim), Double.parseDouble(scale),
				Double.parseDouble(offset));
	}

	/**
	 * Parse an dimension string and intern it: dim := <ratio> ["*" <ratio>]*
	 * ratio := <base> <exp> base := "kg" | "m" | "sec" | "K" | "A" | "mol" |
	 * "cd"
	 */
	private static Dim parseDim(String s) {
		// handle empty string as dimensionless
		if (s.length() == 0)
			return dimensionless;

		// parse dimension
		Dim dim = new Dim();
		List ratios = split(s, (long) '*', true);
		for (int i = 0; i < ratios.size(); ++i) {
			String r = (String) ratios.get(i);
			if (r.startsWith("kg")) {
				dim.kg = Byte.parseByte(r.substring(2).trim());
				continue;
			}
			if (r.startsWith("sec")) {
				dim.sec = Byte.parseByte(r.substring(3).trim());
				continue;
			}
			if (r.startsWith("mol")) {
				dim.mol = Byte.parseByte(r.substring(3).trim());
				continue;
			}
			if (r.startsWith("m")) {
				dim.m = Byte.parseByte(r.substring(1).trim());
				continue;
			}
			if (r.startsWith("K")) {
				dim.K = Byte.parseByte(r.substring(1).trim());
				continue;
			}
			if (r.startsWith("A")) {
				dim.A = Byte.parseByte(r.substring(1).trim());
				continue;
			}
			if (r.startsWith("cd")) {
				dim.cd = Byte.parseByte(r.substring(2).trim());
				continue;
			}
			throw new MeasurementException("Bad ratio '" + r + "'");
		}

		// intern
		return dim.intern();
	}

	/**
	 * Private constructor.
	 */
	private ObixUnit(List<String> ids, Dim dim, double scale, double offset) {
		this.ids = checkIds(ids);
		this.dim = dim;
		this.scale = scale;
		this.offset = offset;
	}

	static List checkIds(List ids) {
		if (ids.size() == 0)
			throw new MeasurementException("No unit ids defined");
		for (int i = 0; i < ids.size(); ++i)
			checkId((String) ids.get(i));
		return (List) Collections.unmodifiableList(ids);
	}

	static void checkId(String id) {
		if (id.length() == 0)
			throw new MeasurementException("Invalid unit id length 0");
		for (int i = 0; i < id.length(); ++i) {
			int c = id.charAt(i);
			if (Character.isAlphabetic(c) || c == '_' || c == '%' || c == '/'
					|| c == '$' || c > 128)
				continue;
			throw new MeasurementException("Invalid unit id " + id + " (invalid char '"
					+ (char) c + "')");
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	// Identity
	// ////////////////////////////////////////////////////////////////////////

	public final boolean equals(Object obj) {
		return this == obj;
	}

	public final int hashCode() {
		return toString().hashCode();
	}

	public final long hash() {
		return Objects.hashCode(toString());
	}

	final Class typeof() {
		return ObixUnit.class; 
	}

	public final String toString() {
		return (String) ids.get(ids.size()-1) ;
	}

	public final List ids() {
		return ids;
	}

	public final String getName() {
		return (String) ids.get(0);
	}

	public final String getSymbol() {
		return (String) ids.get(ids.size()-1) ;
	}

	public final double scale() {
		return scale;
	}

	public final double offset() {
		return offset;
	}

	public final String getDescription() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < ids.size(); ++i) {
			if (i > 0)
				s.append(", ");
			s.append(ids.get(i));
		}
		if (dim != dimensionless) {
			s.append("; ").append(dim);
			if (scale != 1.0 || offset != 0.0) {
				s.append("; ").append(scale);
				if (offset != 0.0)
					s.append("; ").append(offset);
			}
		}
		return s.toString();
	}

	// ////////////////////////////////////////////////////////////////////////
	// Dimension
	// ////////////////////////////////////////////////////////////////////////

	public final long kg() {
		return dim.kg;
	}

	public final long m() {
		return dim.m;
	}

	public final long sec() {
		return dim.sec;
	}

	public final long K() {
		return dim.K;
	}

	public final long A() {
		return dim.A;
	}

	public final long mol() {
		return dim.mol;
	}

	public final long cd() {
		return dim.cd;
	}

	static final class Dim implements Dimension {
		public int hashCode() {
			return (kg << 28) ^ (m << 23) ^ (sec << 18) ^ (K << 13) ^ (A << 8)
					^ (mol << 3) ^ cd;
		}

		public boolean equals(Object o) {
			Dim x = (Dim) o;
			return kg == x.kg && m == x.m && sec == x.sec && K == x.K
					&& A == x.A && mol == x.mol && cd == x.cd;
		}

		public String toString() {
			if (str == null) {
				StringBuilder s = new StringBuilder();
				append(s, "kg", kg);
				append(s, "m", m);
				append(s, "sec", sec);
				append(s, "K", K);
				append(s, "A", A);
				append(s, "mol", mol);
				append(s, "cd", cd);
				str = s.toString();
			}
			return str;
		}

		private void append(StringBuilder s, String key, int val) {
			if (val == 0)
				return;
			if (s.length() > 0)
				s.append('*');
			s.append(key).append(val);
		}

		public Dim add(Dim b) {
			Dim r = new Dim();
			r.kg = (byte) (kg + b.kg);
			r.m = (byte) (m + b.m);
			r.sec = (byte) (sec + b.sec);
			r.K = (byte) (K + b.K);
			r.A = (byte) (A + b.A);
			r.mol = (byte) (mol + b.mol);
			r.cd = (byte) (cd + b.cd);
			return r;
		}

		public Dim subtract(Dim b) {
			Dim r = new Dim();
			r.kg = (byte) (kg - b.kg);
			r.m = (byte) (m - b.m);
			r.sec = (byte) (sec - b.sec);
			r.K = (byte) (K - b.K);
			r.A = (byte) (A - b.A);
			r.mol = (byte) (mol - b.mol);
			r.cd = (byte) (cd - b.cd);
			return r;
		}

		public Dim intern() {
			// intern
			synchronized (dims) {
				Dim cached = (Dim) dims.get(this);
				if (cached != null)
					return cached;
				dims.put(this, this);
				return this;
			}
		}

		public boolean isDimensionless() {
			return toString().length() == 0;
		}

		String str;
		byte kg, m, sec, K, A, mol, cd;

		@Override
		public Dimension divide(Dimension arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<? extends Dimension, Integer> getProductDimensions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Dimension multiply(Dimension arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Dimension pow(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Dimension root(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	// Arithmetic
	// ////////////////////////////////////////////////////////////////////////

	public final Unit multiply(Unit b) {
		synchronized (combos) {
			Combo key = new Combo(this, "*", b);
			Unit r = (ObixUnit) combos.get(key);
			if (r == null) {
				r = findMult(this, b);
				combos.put(key, r);
			}
			return r;
		}
	}

	private static Unit findMult(Unit x, Unit y) {
		if (x instanceof ObixUnit && y instanceof ObixUnit) {
			final ObixUnit a = (ObixUnit)x;
			final ObixUnit b = (ObixUnit)y;

			// if either is dimensionless give up immediately
			if (a.dim.isDimensionless() || b.dim.isDimensionless())
				throw new MeasurementException("Cannot compute dimensionless: " + a + " * " + b);
	
			// compute dim/scale of a * b
			Dim dim = a.dim.add(b.dim).intern();
			double scale = a.scale * b.scale;
	
			// find all the matches
			ObixUnit[] matches = match(dim, scale);
			if (matches.length == 1)
				return matches[0];
	
			// right how our technique for resolving multiple matches is lame
			String expectedName = a.getName() + "_" + b.getName();
			for (int i = 0; i < matches.length; ++i)
				if (matches[i].getName().equals(expectedName))
					return matches[i];
	
			// for now just give up
			throw new MeasurementException("Cannot match to db: " + a + " * " + b);
		} else {
			throw new IllegalArgumentException("Cannot match: " + x.getClass() + " / " + y.getClass());
		}
	}

	@Override
	public final Unit divide(Unit b) {
		synchronized (combos) {
			Combo key = new Combo(this, "/", b);
			Unit r = (ObixUnit) combos.get(key);
			if (r == null) {
				r = findDiv(this, b);
				combos.put(key, r);
			}
			return r;
		}
	}

	final Unit findDiv(Unit x, Unit y) {
		if (x instanceof ObixUnit && y instanceof ObixUnit) {
			final ObixUnit a = (ObixUnit)x;
			final ObixUnit b = (ObixUnit)y;
			// if either is dimensionless give up immediately
			if (a.dim.isDimensionless() || b.dim.isDimensionless())
				throw new MeasurementException("Cannot compute dimensionless: " + a + " / " + b);
	
			// compute dim/scale of a / b
			Dim dim = a.dim.subtract(b.dim).intern();
			double scale = a.scale / b.scale;
	
			// find all the matches
			ObixUnit[] matches = match(dim, scale);
			if (matches.length == 1)
				return matches[0];
	
			// right how our technique for resolving multiple matches is lame
			String expectedName = a.getName() + "_per_" + b.getName();
			for (int i = 0; i < matches.length; ++i)
				if (matches[i].getName().contains(expectedName))
					return matches[i];
	
			// for now just give up
			throw new MeasurementException("Cannot match to db: " + a + " / " + b);
		} else {
			throw new IllegalArgumentException("Cannot match: " + x.getClass() + " / " + y.getClass());
		}
	}

	private static ObixUnit[] match(Dim dim, double scale) {
		ArrayList acc = new ArrayList();
		synchronized (list) {
			for (int i = 0; i < list.size(); ++i) {
				ObixUnit x = (ObixUnit) list.get(i);
				if (x.dim == dim && approx(x.scale, scale))
					acc.add(x);
			}
		}
		return (ObixUnit[]) acc.toArray(new ObixUnit[acc.size()]);
	}

	private static boolean approx(double a, double b) {
		// pretty loose with our approximation because the database
		// doesn't have super great resolution for some normalizations
		if (a == b)
			return true;
		double t = Math.min(Math.abs(a / 1e3), Math.abs(b / 1e3));
		return Math.abs(a - b) <= t;
	}

	static final class Combo {
		Combo(Unit a, String op, Unit b) {
			this.a = a;
			this.op = op;
			this.b = b;
		}

		public int hashCode() {
			return a.hashCode() ^ op.hashCode() ^ (b.hashCode() << 13);
		}

		public boolean equals(Object that) {
			Combo x = (Combo) that;
			return a == x.a && op == x.op && b == x.b;
		}

		final Unit a;
		final String op;
		final Unit b;
	}

	// ////////////////////////////////////////////////////////////////////////
	// Conversion
	// ////////////////////////////////////////////////////////////////////////

	public final double convertTo(double scalar, ObixUnit to) {
		if (dim != to.dim)
			throw new MeasurementException("Incovertable units: " + this + " and " + to);
		return ((scalar * this.scale + this.offset) - to.offset) / to.scale;
	}

	// ////////////////////////////////////////////////////////////////////////
	// Fields
	// ////////////////////////////////////////////////////////////////////////

	private static final List<ObixUnit> list = new ArrayList<>();
	private static final HashMap byId = new HashMap(); // String id-> Unit
	private static final Map<Dimension, Dimension> dims = new HashMap<>(); // Dimension -> Dimension
	private static final Map quantities = new HashMap(); // String -> List
	private static final HashMap combos = new HashMap(); // Combo -> Unit
	private static final List<String> quantityNames;
	private static final Dim dimensionless = new Dim();
	static {
		dims.put(dimensionless, dimensionless); // TODO use impl and DIMENSIONLESS definition
		quantityNames = loadDatabase();
	}

	@Override
	public Dimension getDimension() {
		return dim;
	}
	
	private final List ids;
	private final double scale;
	private final double offset;
	private final Dim dim;
	
	@Override
	public Unit alternate(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit asType(Class arg0) throws ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit divide(double arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnitConverter getConverterTo(Unit arg0)
			throws UnconvertibleException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnitConverter getConverterToAny(Unit arg0)
			throws IncommensurableException, UnconvertibleException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getProductUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit getSystemUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit inverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompatible(Unit arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Unit multiply(double arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit pow(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit root(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit shift(double arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit transform(UnitConverter arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}