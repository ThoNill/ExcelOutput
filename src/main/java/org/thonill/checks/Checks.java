package org.thonill.checks;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import org.thonill.logger.LOG;

/**
 * This class provides utility methods for performing validation checks.
 *
 * Checks if the given file exists.
 *
 * @param fileName The path to the file to check.
 * @param where    A description of where the file is expected to exist.
 * @param what     A description of what the file represents.
 * @throws IllegalArgumentException if the file does not exist.
 */
public class Checks {

	private Checks() {
		super();
	}

	public static File checkFileExists(String fileName, String where, String what) {
		if (fileName == null) {
			return null;
		}
		File file = new File(fileName);
		LOG.info(file.getAbsolutePath());
		checkArgument(new File(fileName).exists(),
				"" + where + ": the file " + what + " with the name " + fileName + " does not exist");
		return file;
	}
}
