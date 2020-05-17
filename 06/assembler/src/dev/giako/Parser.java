package dev.giako;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulate access to source code. Reads an assembly language command, parses it, and provides convenient access to
 * the command's components (fields and symbols). In addition, removes all whitespace and comments.
 */
public class Parser {
    private static final Pattern C_COMMAND = Pattern.compile("((\\w+)=)?(\\w+)(;(\\w+))?");
    private static final Pattern A_COMMAND = Pattern.compile("@(\\w+)");
    private static final Pattern L_COMMAND = Pattern.compile("\\((\\w+)\\)");
    private final BufferedReader bufferedReader;
    private String line;

    /**
     * Opens the input file/stream and gets ready to parse it
     *
     * @param bufferedReader file reader to parse
     */
    public Parser(BufferedReader bufferedReader) throws IOException {
        this.bufferedReader = bufferedReader;
        advance();
    }

    /**
     * Are there more commands in the input?
     *
     * @return true if there are any commands
     */
    public boolean hasMoreCommands() {
        return line != null;
    }

    /**
     * Reads the next command from the input and makes it the current command. Should be called only if
     * hasMoreCommands() is true. Initially there is no current command.
     */
    public void advance() throws IOException {
        line = bufferedReader.readLine();

        if (line == null) {
            return;
        }

        line = line.replaceAll("//.*", "").trim();

        if (line.length() == 0 && hasMoreCommands()) {
            advance();
        }
    }

    /**
     * Returns the type of the current command
     *
     * @return the current command type
     */
    public CommandType getCommandType() {
        if (L_COMMAND.matcher(line).matches()) {
            return CommandType.L_COMMAND;
        } else if (A_COMMAND.matcher(line).matches()) {
            return CommandType.A_COMMAND;
        } else if (C_COMMAND.matcher(line).matches()) {
            return CommandType.C_COMMAND;
        } else {
            throw new UnsupportedOperationException("Cannot recognize command");
        }
    }

    /**
     * Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx). Should be called only when
     * getCommandType() is A_COMMAND or L_COMMAND.
     *
     * @return the symbol or decimal
     */
    public String getSymbol() {
        switch (getCommandType()) {
            case A_COMMAND:
                return A_COMMAND.matcher(line).group(1);

            case L_COMMAND:
                return L_COMMAND.matcher(line).group(1);

            default:
                throw new UnsupportedOperationException("Current command is neither A-command or L-command");
        }
    }

    /**
     * Returns the dest mnemonic in the current C-command (8 possibilities). Should be called only when getCommandType()
     * is C_COMMAND.
     *
     * @return the dest mnemonic
     */
    public String getDest() {
        // dest=comp;jump
        // dest=comp
        // comp;jump
        // comp
        Matcher matcher = C_COMMAND.matcher(line);
        var dest = matcher.group(2);

        if (dest.length() == 0) {
            return "null";
        }

        return dest;
    }

    /**
     * Returns the comp mnemonic in the current C-command (28 possibilities). Should be called only when
     * getCommandType() is C_COMMAND.
     *
     * @return the comp mnemonic
     */
    public String getComp() {
        // dest=comp;jump
        // dest=comp
        // comp;jump
        // comp
        Matcher matcher = C_COMMAND.matcher(line);

        return matcher.group(3);
    }

    /**
     * Returns the jump mnemonic in the current C-command (8 possibilities). Should be called only when commandType() is
     * C_COMMAND.
     *
     * @return the jump mnemonic
     */
    public String getJump() {
        // dest=comp;jump
        // dest=comp
        // comp;jump
        // comp
        Matcher matcher = C_COMMAND.matcher(line);
        var jump = matcher.group(5);

        if (jump.length() == 0) {
            return "null";
        }

        return jump;
    }
}
