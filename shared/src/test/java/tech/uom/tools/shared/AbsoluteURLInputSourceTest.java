package tech.uom.tools.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Anakar Parida
 * Date: 5/13/15
 * Time: 11:16 PM
 */
public class AbsoluteURLInputSourceTest {
	
	@Test
	public void getInputTest(){
		AbsoluteURLInputSource absUrlInputSrc = new AbsoluteURLInputSource(null);
		try {
			final InputStream result = absUrlInputSrc.getInput("");
			assertNotNull(result);
			assertEquals(0, result.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
