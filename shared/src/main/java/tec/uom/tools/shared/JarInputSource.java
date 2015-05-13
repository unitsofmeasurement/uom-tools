package tec.uom.tools.shared;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Anakar Parida
 * Date: 5/13/15
 * Time: 11:16 PM
 */
public class JarInputSource implements InputSource{

	private static final String DEFAULT_PATH = "/";
	
	private final String path;
	
	public JarInputSource(String path) {
		if( null == path){
			this.path = DEFAULT_PATH;
		}else{
			this.path = path;
		}
	}

	@Override
	public InputStream getInput(String source) throws IOException {
		String jarPath = path + source;
		InputStream inputStream = JarInputSource.class.getResourceAsStream(jarPath);
		
		if(null == inputStream){
			throw new FileNotFoundException("Jar not found: " + jarPath);
		}
		return inputStream;
	}

}
