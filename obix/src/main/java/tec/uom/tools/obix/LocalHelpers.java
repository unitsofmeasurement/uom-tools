package tec.uom.tools.obix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class LocalHelpers {
	static final List dup(Object[] values, int size)
	  {
	    Object[] dup = new Object[size];
	    System.arraycopy(values, 0, dup, 0, size);
	    return Arrays.asList(dup);
	  }
	
	public static List split(String self) { return split(self, null, true); }
	  public static List split(String self, Long separator) { return split(self, separator, true); }
	  public static List split(String self, Long separator, boolean trimmed)
	  {
	    if (separator == null) return splitws(self);
	    int sep = separator.intValue();
	    boolean trim = trimmed;
	    List toks = new ArrayList<String>(16);
	    int len = self.length();
	    int x = 0;
	    for (int i=0; i<len; ++i)
	    {
	      if (self.charAt(i) != sep) continue;
	      if (x <= i) toks.add(splitStr(self, x, i, trim));
	      x = i+1;
	    }
	    if (x <= len) toks.add(splitStr(self, x, len, trim));
	    return toks;
	  }

	  private static String splitStr(String val, int s, int e, boolean trim)
	  {
	    if (trim)
	    {
	      while (s < e && val.charAt(s) <= ' ') ++s;
	      while (e > s && val.charAt(e-1) <= ' ') --e;
	    }
	    return val.substring(s, e);
	  }

	  public static List splitws(String val)
	  {
	    List toks = new ArrayList<String>(16);
	    int len = val.length();
	    while (len > 0 && val.charAt(len-1) <= ' ') --len;
	    int x = 0;
	    while (x < len && val.charAt(x) <= ' ') ++x;
	    for (int i=x; i<len; ++i)
	    {
	      if (val.charAt(i) > ' ') continue;
	      toks.add(val.substring(x, i));
	      x = i + 1;
	      while (x < len && val.charAt(x) <= ' ') ++x;
	      i = x;
	    }
	    if (x <= len) toks.add(val.substring(x, len));
	    if (toks.size() == 0) toks.add("");
	    return toks;
	  }

	  public static List splitLines(String self)
	  {
	    List lines = new ArrayList<String>(16);
	    int len = self.length();
	    int s = 0;
	    for (int i=0; i<len; ++i)
	    {
	      int c = self.charAt(i);
	      if (c == '\n' || c == '\r')
	      {
	        lines.add(self.substring(s, i));
	        s = i+1;
	        if (c == '\r' && s < len && self.charAt(s) == '\n') { i++; s++; }
	      }
	    }
	    lines.add(self.substring(s, len));
	    return lines;
	  }
}
