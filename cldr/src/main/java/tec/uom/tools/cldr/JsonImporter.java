package tec.uom.tools.cldr;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.Tool;

public class JsonImporter implements Tool {

	@Override
	public int run(InputStream in, OutputStream out, OutputStream err,
			String... arguments) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public final Set<SourceVersion> getSourceVersions() {
		return Collections.unmodifiableSet(new HashSet<SourceVersion>(Arrays.asList(
				new SourceVersion[]{SourceVersion.RELEASE_5, SourceVersion.RELEASE_6, 
						SourceVersion.RELEASE_7 }
				)));
	}

}
