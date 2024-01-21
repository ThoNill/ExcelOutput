package org.thonill.replace;

import java.util.ArrayList;
import java.util.List;

import org.thonill.exceptions.ApplicationException;
import org.thonill.logger.LOG;

/**
 * This class splits SQL text into individual SQL statements. It parses the
 * input SQL text character by character, tracking when it enters and exits
 * comments, string constants, etc. to identify statement boundaries.
 */

public class SplitSqlText {

	StringBuilder sb = new StringBuilder();

	/**
	 * This enum represents the parsing status when splitting SQL text into
	 * statements. IN_SQL - Currently parsing a SQL statement IN_LINE_COMMENT -
	 * Currently inside a line comment (--) IN_COMMENT - Currently inside a block
	 * comment ( / * * / END_COMMENT - Just ended a block comment* IN_CONSTANT1 -
	 * Currently inside a single-quoted string constant ('') IN_CONSTANT2 -
	 * Currently inside a double-quoted string constant ("") STOP_SQL - Reached end
	 * of a SQL statement START_NEW_SQL_LINE - At start, ready to begin parsing a
	 * new line in a statement
	 */
	enum Status {
		IN_SQL, STOP_SQL, START_NEW_SQL_LINE, IN_LINE_COMMENT, IN_COMMENT, END_COMMENT, IN_CONSTANT1, IN_CONSTANT2
	}

	private Status status = Status.START_NEW_SQL_LINE;

	public List<RawSqlStatement> extractList(String sqls) {
		sb = new StringBuilder();
		List<RawSqlStatement> statements = new ArrayList<>();
		char[] chars = sqls.toCharArray();
		status = Status.START_NEW_SQL_LINE;
		for (int i = 0; i < chars.length; i++) {
			char vc = vorherigesZeichen(chars, i);
			char c = chars[i];
			doChar(statements, vc, c);
		}
		addSqlStatement(statements);

		if (status == Status.IN_CONSTANT1 || status == Status.IN_CONSTANT2 || status == Status.IN_COMMENT) {
			throw new ApplicationException("the Text " + sqls + " can not be splitted");
		}

		return statements;
	}

	private char vorherigesZeichen(char[] chars, int i) {
		return (i == 0 || status == Status.START_NEW_SQL_LINE) ? ' ' : chars[i - 1];
	}

	private void doChar(List<RawSqlStatement> statements, char vc, char c) {
		LOG.info("c= {0}  status= {1} ", (zeilenende(c) ? "\\n" : "" + c), status.name());
		if (als_Leerzeichen_anzusehen(c)) {
			c = ' ';
		}
		switch (this.status) {
		case IN_SQL:
			parsingStatement(statements, vc, c);
			break;
		case IN_LINE_COMMENT:
			parsingLineComment(c);
			break;
		case IN_COMMENT:
			parsingComment(vc, c);
			break;
		case END_COMMENT:
			neuerZeilenAnfang(c);
			break;
		case IN_CONSTANT1:
			parsingConstante1(c);
			break;
		case IN_CONSTANT2:
			parsingConstante2(c);
			break;
		case STOP_SQL:
			addSqlStatement(statements);
			neuesStatement(c);
			break;
		case START_NEW_SQL_LINE:
			parseZeilenanfang(statements, c);
			break;

		}
		LOG.info(" to status= {0}", status.name());
	}

	private void neuerZeilenAnfang(char c) {
		if (zeilenende(c)) {
			status = Status.START_NEW_SQL_LINE;
		} else {
			sb.append(c);
			status = Status.IN_SQL;
		}
	}

	private void parseZeilenanfang(List<RawSqlStatement> statements, char c) {
		if (zeilenende(c)) {
			status = Status.STOP_SQL;
		} else if (endeEinesSelects(c)) {
			addSqlStatement(statements);
		} else if (hat_vielleicht_mit_Kommentaren_zu_tun(c)) {
			status = Status.IN_SQL;
		} else if (keinLeerzeichen(c)) {
			status = Status.IN_SQL;
			sb.append(c);
		} else {
			sb.append(c);
		}
	}

	private void neuesStatement(char c) {
		if (zeilenende(c)) {
			status = Status.STOP_SQL;
		} else {
			status = Status.IN_SQL;
			sb.append(c);
		}
	}

	// Kontante Strings werden erhalten
	private void parsingConstante2(char c) {
		if (beginConstante2(c)) {
			status = Status.IN_SQL;
		}
		sb.append(c);
	}

	// Kontante Strings werden erhalten
	private void parsingConstante1(char c) {
		if (beginConstante1(c)) {
			status = Status.IN_SQL;
		}
		sb.append(c);
	}

	// Die Zeichen in Kommentaren werden bis zum Ende des Kommentars ignoriert
	private void parsingComment(char vc, char c) {
		if (endeKommentar(vc, c)) {
			status = Status.END_COMMENT;
		}
	}

	// Die Zeichen in Kommentaren werden werden bis zum Ende des Kommentars
	// ignoriert
	private void parsingLineComment(char c) {
		if (zeilenende(c)) {
			status = Status.START_NEW_SQL_LINE;
		}
	}

	private void parsingStatement(List<RawSqlStatement> statements, char vc, char c) {
		if (startZeilenkommentar(vc, c)) { // --
			status = Status.IN_LINE_COMMENT;
		} else if (starteKommentar(vc, c)) { // /*
			status = Status.IN_COMMENT;
		} else if (beginConstante1(c)) { // '
			status = Status.IN_CONSTANT1;
			sb.append(c);
		} else if (beginConstante2(c)) { // "
			status = Status.IN_CONSTANT2;
			sb.append(c);
		} else if (endeEinesSelects(c)) { // ;
			addSqlStatement(statements);
		} else if (hat_vielleicht_mit_Kommentaren_zu_tun(c)) { // - / *
			doNothing();
		} else if (falls_es_dann_mit_Kommentaren_zu_tun_hat(vc, c)) { // -- /* */
			sb.append(vc);
			sb.append(c);
		} else if (zeilenende(c)) { // \n
			status = Status.START_NEW_SQL_LINE;
		} else {
			sb.append(c);
		}
	}

	private boolean endeKommentar(char vc, char c) {
		return vc == '*' && c == '/';
	}

	private boolean keinLeerzeichen(char c) {
		return c != ' ';
	}

	private boolean als_Leerzeichen_anzusehen(char c) {
		return c == '\r' || c == '\t';
	}

	private boolean falls_es_dann_mit_Kommentaren_zu_tun_hat(char vc, char c) {
		return (vc == '-' && c != '-') || (vc == '/' && c != '*') || (vc == '*' && c != '/');
	}

	private boolean hat_vielleicht_mit_Kommentaren_zu_tun(char c) {
		return c == '-' || c == '/' || c == '*';
	}

	private boolean zeilenende(char c) {
		return c == '\n';
	}

	private boolean endeEinesSelects(char c) {
		return c == ';';
	}

	private boolean beginConstante2(char c) {
		return c == '\"';
	}

	private boolean beginConstante1(char c) {
		return c == '\'';
	}

	private boolean starteKommentar(char vc, char c) {
		return vc == '/' && c == '*';
	}

	private boolean startZeilenkommentar(char vc, char c) {
		return vc == '-' && c == '-';
	}

	private void doNothing() {
		// Mach nichts
	}

	private void addSqlStatement(List<RawSqlStatement> statements) {
		String sqlStatement = sb.toString().trim();
		if (sqlStatement.length() > 0) {
			statements.add(new RawSqlStatement(sqlStatement));
		}
		sb.setLength(0);
	}

}
