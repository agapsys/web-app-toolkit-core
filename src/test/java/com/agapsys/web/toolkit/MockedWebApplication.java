/* 
 * Copyright (C) 2015 Agapsys Tecnologia - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.agapsys.web.toolkit;

import com.agapsys.web.toolkit.utils.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.annotation.WebListener;

@WebListener
public class MockedWebApplication extends AbstractWebApplication {
	private File appFolder = null;
	
	@Override
	public String getName() {
		return "test-app";
	}

	@Override
	public String getVersion() {
		return "0.1.0";
	}
	
	@Override
	protected String getDirectoryAbsolutePath() {
		if (appFolder == null) {
			try {
				appFolder = FileUtils.getRandomNonExistentFile(FileUtils.DEFAULT_TEMPORARY_FOLDER, 8, 1000);
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		
		return appFolder.getAbsolutePath();
	}

	@Override
	protected void afterApplicationStop() {
		super.afterApplicationStop();
		
		try {
			for (File file : appFolder.listFiles()) {
				Files.delete(file.toPath());
			}

			Files.delete(appFolder.toPath());
			appFolder = null;
		} catch (IOException ex) {
			appFolder = null;
			throw new RuntimeException(ex);
		}
	}
}
