package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thonill.gui.FieldDescription;

public class VariableExtractor {

	public static List<FieldDescription> extractFieldDescriptionsFromFile(String sqlFile) throws IOException {
		checkNotNull(sqlFile, "VeriableExtractor.extractFieldsFromFile sqlFile is null");
		checkFileExists(sqlFile, "ExcelOutputApplication.main", sqlFile + " File does not exist");
		String querys = Files.readString(Paths.get(sqlFile));
		return extractFieldDescriptions(querys);
	}

	private static List<FieldDescription> extractFieldDescriptions(String original) {
		List<FieldDescription> keys = new ArrayList<>();
		String regexpr = "\\{([^\\{\\}]*)\\}";

		Pattern pattern = Pattern.compile(regexpr);

		Matcher matcher = pattern.matcher(original);
		while (matcher.find()) {
			String fieldName = original.substring(matcher.start() + 1, matcher.end() - 1);
			keys.add(new FieldDescription(fieldName, "",
					"" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1).toLowerCase()));
		}
		return keys;
	}

}
