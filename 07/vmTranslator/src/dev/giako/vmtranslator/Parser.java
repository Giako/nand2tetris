package dev.giako.vmtranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Handle the parsing of a single .vm file, and encapsulates access to the input code. It reads VM commands, parses
 * them, and provides convenient access to their components. In addition, it removes all white space and comments.
 */
public class Parser {
    private final BufferedReader reader;
    private String line;

    private static final Pattern C_ARITHMETIC = Pattern.compile("(add|sub|neg|eq|gt|lt|and|or|not)");
    private static final Pattern C_PUSH =
            Pattern.compile("push (argument|local|static|constant|this|that|pointer|temp) (\\d+)");
    private static final Pattern C_POP =
            Pattern.compile("pop (argument|local|static|constant|this|that|pointer|temp) (\\d+)");
    private static final Pattern C_LABEL = Pattern.compile("label ([\\w.:$]+)");
    private static final Pattern C_GOTO = Pattern.compile("goto ([\\w.:$]+)");
    private static final Pattern C_IF = Pattern.compile("if-goto ([\\w.:$]+)");
    private static final Pattern C_FUNCTION = Pattern.compile("function ([\\w.:$]+) (\\d+)");
    private static final Pattern C_CALL = Pattern.compile("call ([\\w.:$]+) (\\d+)");
    private static final Pattern C_RETURN = Pattern.compile("return");

    /**
     * Opens the input file/stream and gets ready to parse it.
     *
     * @param reader the file to parse
     */
    public Parser(BufferedReader reader) throws IOException {
        this.reader = reader;
        advance();
    }

    /**
     * Are there more commands in the input?
     *
     * @return true if there are other commands
     */
    public boolean hasMoreCommands() {
        return line != null;
    }

    /**
     * Reads the next command from the input and makes it the current command. Should be called only if
     * hasMoreCommands() is true. Initially there is no current command.
     */
    public void advance() throws IOException {
        line = reader.readLine();

        if (line == null) {
            return;
        }

        line = line.replaceAll("//.*", "").trim();

        if (line.length() == 0 && hasMoreCommands()) {
            advance();
        }
    }

    /**
     * Returns the type of the current VM command. C_ARITHMETIC is returned for all arithmetic commands.
     *
     * @return the current VM command type
     */
    public VmCommand getCommandType() {
        if (C_ARITHMETIC.matcher(line).matches()) {
            return VmCommand.C_ARITHMETIC;
        } else if (C_PUSH.matcher(line).matches()) {
            return VmCommand.C_PUSH;
        } else if (C_POP.matcher(line).matches()) {
            return VmCommand.C_POP;
        } else if (C_LABEL.matcher(line).matches()) {
            return VmCommand.C_LABEL;
        } else if (C_GOTO.matcher(line).matches()) {
            return VmCommand.C_GOTO;
        } else if (C_IF.matcher(line).matches()) {
            return VmCommand.C_IF;
        } else if (C_FUNCTION.matcher(line).matches()) {
            return VmCommand.C_FUNCTION;
        } else if (C_CALL.matcher(line).matches()) {
            return VmCommand.C_CALL;
        } else if (C_RETURN.matcher(line).matches()) {
            return VmCommand.C_RETURN;
        } else {
            throw new IllegalArgumentException(String.format("Cannot determine command type for: %s", line));
        }
    }

    /**
     * Returns the first argument of the current command. In the case of C_ARITHMETIC the command itself (add, sub,
     * etc.) is returned. Should not be called if the current command is C_RETURN.
     *
     * @return the first argument as a string
     */
    public String getArg1() {
        switch (getCommandType()) {
            case C_ARITHMETIC:
                return getGroupInCurrentLine(C_ARITHMETIC, 1);

            case C_PUSH:
                return getGroupInCurrentLine(C_PUSH, 1);

            case C_POP:
                return getGroupInCurrentLine(C_POP, 1);

            case C_LABEL:
                return getGroupInCurrentLine(C_LABEL, 1);

            case C_GOTO:
                return getGroupInCurrentLine(C_GOTO, 1);

            case C_IF:
                return getGroupInCurrentLine(C_IF, 1);

            case C_FUNCTION:
                return getGroupInCurrentLine(C_FUNCTION, 1);

            case C_CALL:
                return getGroupInCurrentLine(C_CALL, 1);

            case C_RETURN:
                throw new IllegalArgumentException("Cannot get arg 1 from a return command");

            default:
                throw new UnsupportedOperationException("Command not supported");
        }
    }

    private String getGroupInCurrentLine(Pattern pattern, int groupNumber) {
        var matcher = pattern.matcher(line);
        //noinspection ResultOfMethodCallIgnored
        matcher.matches();

        return matcher.group(groupNumber);
    }

    /**
     * Returns the second argument of the current command. Should be called only if the current command is C_PUSH,
     * C_POP, C_FUNCTION, or C_CALL.
     *
     * @return the second argument as an integer
     */
    public int getArg2() {
        switch (getCommandType()) {
            case C_PUSH:
                return Integer.parseInt(getGroupInCurrentLine(C_PUSH, 2));

            case C_POP:
                return Integer.parseInt(getGroupInCurrentLine(C_POP, 2));

            case C_FUNCTION:
                return Integer.parseInt(getGroupInCurrentLine(C_FUNCTION, 2));

            case C_CALL:
                return Integer.parseInt(getGroupInCurrentLine(C_CALL, 2));

            default:
                throw new IllegalArgumentException("Cannot get arg 2 from command types that are not push, pop, " +
                        "function or call");
        }
    }
}
