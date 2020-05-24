package dev.giako.vmtranslator;

import java.io.Closeable;
import java.io.PrintWriter;

/**
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter implements Closeable {
    private PrintWriter printWriter;
    private String fileName;

    /**
     * Opens the input file/stream and gets ready to write into it.
     * @param printWriter a PrintWriter for writing the output file
     */
    public CodeWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    /**
     * Informs the code writer that the translation of a new VM file is started.
     * @param fileName the name of the file to write
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Writes the assembly code that is the translation of the given arithmetic command.
     * @param command the arithmetic command to translate
     */
    public void writeArithmetic(String command) {
        // TODO Implement
    }

    /**
     * Writes the assembly code that is the translation of the given command, where command is either C_PUSH or C_POP.
     * @param command the command type, only C_PUSH or C_POP are supported
     * @param segment the segment argument of the command
     * @param index the index argument of the command
     */
    public void writePushPop(VmCommand command, String segment, int index) {
        // TODO Implement
    }

    /**
     * Closes the output file.
     */
    public void close() {
        printWriter.close();
    }

    // More routines will be added to this module in chapter 8
}
