package org.thonill.replace;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class splits SQL text into individual SQL statements.
 * It parses the input SQL text character by character, tracking
 * when it enters and exits comments, string constants, etc. to
 * identify statement boundaries.
 */

public class SplitSqlText {
    private static final Logger LOG = Logger.getLogger(SplitSqlText.class.getName());

    StringBuilder sb = new StringBuilder();

    /**
     * This enum represents the parsing status when splitting SQL text into
     * statements.
     * IN_SQL - Currently parsing a SQL statement
     * IN_LINE_COMMENT - Currently inside a line comment (--)
     * IN_COMMENT - Currently inside a block comment ( / * * /
     * END_COMMENT- Just ended a block comment*
     * IN_CONSTANT1- Currently inside a single-quoted string constant ('')
     * IN_CONSTANT2 - Currently inside a double-quoted string constant ("")
     * STOP_SQL - Reached end of a SQL statement
     * START_SQL - At start, ready to begin parsing a new statement
     */
    enum Status {
        IN_SQL,
        IN_LINE_COMMENT,
        IN_COMMENT,
        END_COMMENT,
        IN_CONSTANT1,
        IN_CONSTANT2,
        STOP_SQL,
        START_SQL
    };

    private Status status = Status.START_SQL;

    public List<RawSqlStatement> extractList(String sqls) {
        sb = new StringBuilder();
        List<RawSqlStatement> statements = new ArrayList<>();
        char[] chars = sqls.toCharArray();
        status = Status.START_SQL;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            char lc = (i > 0 && status != Status.START_SQL) ? chars[i - 1] : ' ';
            doChar(statements, c, lc);
        }
        addSqlStatement(statements);
        return statements;
    }

    private void doChar(List<RawSqlStatement> statements, char c, char lc) {
        LOG.info("c=" + ((c == '\n') ? "\\n" : "" + c) + " status=" + status.name());
        if (c == '\r' || c == '\t') {
            c = ' ';
        }
        switch (this.status) {
            case IN_SQL:
                if (lc == '-' && c == '-') {
                    status = Status.IN_LINE_COMMENT;
                } else if (lc == '/' && c == '*') {
                    status = Status.IN_COMMENT;
                } else if (c == '\'') {
                    status = Status.IN_CONSTANT1;
                    sb.append(c);
                } else if (c == '\"') {
                    status = Status.IN_CONSTANT2;
                    sb.append(c);
                } else if (c == ';') {
                    addSqlStatement(statements);
                } else if (c == '\n') {
                    status = Status.START_SQL;
                } else if (c == '-') {
                } else if (lc == '-' && c != '-') {
                    sb.append('-');
                    sb.append(c);
                } else if (c == '/') {
                } else if (lc == '/' && c != '*') {
                    sb.append('/');
                    sb.append(c);
                } else if (c == '*') {
                } else if (lc == '*' && c != '/') {
                    sb.append('*');
                    sb.append(c);
                } else {
                    sb.append(c);
                }
                break;
            case IN_LINE_COMMENT:
                if (c == '\n') {
                    status = Status.START_SQL;
                }
                break;
            case IN_COMMENT:
                if (lc == '*' && c == '/') {
                    status = Status.END_COMMENT;
                }
                break;
            case END_COMMENT:
                if (c == '\n') {
                    status = Status.START_SQL;
                } else {
                    sb.append(c);
                    status = Status.IN_SQL;
                }
                break;
            case IN_CONSTANT1:
                if (c == '\'') {
                    status = Status.IN_SQL;
                }
                sb.append(c);
                break;
            case IN_CONSTANT2:
                if (c == '\"') {
                    status = Status.IN_SQL;
                }
                sb.append(c);
                break;
            case STOP_SQL:
                addSqlStatement(statements);
                status = Status.IN_SQL;
                if (c == '\n') {
                    status = Status.STOP_SQL;
                } else {
                    sb.append(c);
                }
                break;
            case START_SQL:
                if (c == '\n') {
                    status = Status.STOP_SQL;
                } else if (c == ';') {
                    addSqlStatement(statements);
                } else if (c == '*' || c == '-' || c == '/') {
                    status = Status.IN_SQL;
                } else if (c != ' ') {
                    status = Status.IN_SQL;
                    sb.append(c);
                } else {
                    sb.append(c);
                }
                break;

        }
        LOG.info(" to status=" + status.name());
    }

    private void addSqlStatement(List<RawSqlStatement> statements) {
        String sqlStatement = sb.toString().trim();
        if (sqlStatement.length() > 0) {
            statements.add(new RawSqlStatement(sqlStatement));
        }
        sb.setLength(0);
    }

}
