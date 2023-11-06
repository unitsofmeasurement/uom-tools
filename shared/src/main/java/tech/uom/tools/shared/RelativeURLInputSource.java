package tech.uom.tools.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Anakar Parida
 * Date: 5/13/15
 * Time: 11:16 PM
 */
public class RelativeURLInputSource implements InputSource{
	
	private static final String DEFAULT_PATH = "";
	
	private final String path;
	
	public RelativeURLInputSource(String path) {
		if( null == path){
			this.path = DEFAULT_PATH;
		}else if(path.isEmpty()){
			this.path = "";
		}else{
			this.path = path + File.separatorChar;
		}
	}

	@Override
	public InputStream getInput(String source) throws IOException {
		String relativePath = path + source;
		return new FileInputStream(relativePath);
	}

}
