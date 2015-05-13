package tec.uom.tools.shared;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Anakar Parida
 * Date: 5/13/15
 * Time: 11:16 PM
 */
public interface InputSource {
	public InputStream getInput(String source) throws IOException;
}
