package tec.uom.tools.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Anakar Parida
 * Date: 5/13/15
 * Time: 11:16 PM
 */
public class AbsoluteURLInputSource implements InputSource{
	private static final String DEFAULT_PATH = "http://raw.githubusercontent.com/unicode-cldr/cldr-units-modern/master/main/root/units.json";
	
	private final String path;
	
	public AbsoluteURLInputSource(String path) {
		if( null == path){
			this.path = DEFAULT_PATH;
		}else{
			this.path = path;
		}
	}

	@Override
	public InputStream getInput(String source) throws IOException {
		String absolutePath = path + File.separator + source;
		return new URL(absolutePath).openStream();
	}
}
