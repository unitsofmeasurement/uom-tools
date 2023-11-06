package tech.uom.tools.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import tech.uom.tools.shared.AbsoluteURLInputSource;

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
			InputStream result = absUrlInputSrc.getInput("");
			assertNotNull(result);
			assertEquals(0, result.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
