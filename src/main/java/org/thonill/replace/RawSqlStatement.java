package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.thonill.checks.Checks.checkFileExists;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.thonill.sql.ExecutableStatement;
import org.thonill.sql.ExecutableStatementSet;

/**
 * RawSqlStatement represents a raw SQL query statement. It can parse SQL
 * statements from a file and execute them on a Connection.
 */

public class RawSqlStatement {
	private static final Logger LOG = Logger.getLogger(RawSqlStatement.class.getName());

	private String query;

	public RawSqlStatement(String query) {
		this.query = query;
		checkNotNull(query, "RawSqlStatement.RawSqlStatement: query is null");
	}

	public String getQueryString() {
		return query;
	}

	public static List<RawSqlStatement> getRawSqlStatements(String sqlFile) throws Exception {
		checkNotNull(sqlFile, "RawSqlStatement.getRawSqlStatements: sqlFile is null");
		checkFileExists(sqlFile, "RawSqlStatement.getRawSqlStatements", "sqlFile");
		String querys = Files.readString(Paths.get(sqlFile));
		LOG.info("Querys: " + querys.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
		SplitSqlText splitter = new SplitSqlText();

		return splitter.extractList(querys);

	}

	public ExecutableStatement replaceVariables(List<ReplaceDescription> descriptions) {
		checkNotNull(descriptions, "RawSqlStatement.replaceVariables: descriptions is null");

		ReplaceAccumulator acc = new ReplaceAccumulator(query);
		acc.perform(descriptions);
		return new ExecutableStatement(acc.getText());
	}

	public static List<ReplaceDescription> createReplaceDescriptions(HashMap<String, String> replacements) {
		checkNotNull(replacements, "RawSqlStatement.createReplaceDescriptions: replacements is null");

		List<ReplaceDescription> descriptions = new ArrayList<>();
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			LOG.info("Key: " + entry.getKey() + " Value: " + entry.getValue());
			descriptions.add(new ReplaceDescription(entry.getKey(), entry.getValue()));
		}
		return descriptions;
	}

	public static ExecutableStatementSet replaceVariables(List<RawSqlStatement> rawStatements,
			List<ReplaceDescription> descriptions) {
		checkNotNull(rawStatements, "RawSqlStatement.replaceVariables: rawStatements is null");
		checkNotNull(descriptions, "RawSqlStatement.replaceVariables: descriptions is null");

		ExecutableStatementSet executableStatements = new ExecutableStatementSet();
		for (RawSqlStatement rawStatement : rawStatements) {
			ExecutableStatement executableStatement = rawStatement.replaceVariables(descriptions);
			LOG.info(executableStatement.getQueryString());
			executableStatements.add(executableStatement);
		}
		return executableStatements;
	}

}
