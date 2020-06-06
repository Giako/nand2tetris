package dev.giako.vmtranslator;

import java.io.Closeable;
import java.io.PrintWriter;

/**
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter implements Closeable {
    private final PrintWriter printWriter;
    private String fileName;
    private int labelLogicNumber = 0;

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
        switch (command) {
            case "add":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and add it to register, and store result on second topmost
                // element
                decrementARegister(1); // @SP-2
                printWriter.println("M=D+M");

                decreaseSP();
                break;

            case "sub":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and subtract it from register, and store result on second
                // topmost element
                decrementARegister(1); // @SP-2
                printWriter.println("M=M-D");

                decreaseSP();
                break;

            case "neg":
                addressTopmostStackElementAndStoreInDRegister();

                // Store in topmost element negative register, leave SP unaltered
                printWriter.println("M=-D");
                break;

            case "eq":
                generateLogicInstruction("JEQ");
                break;

            case "gt":
                generateLogicInstruction("JGT");
                break;

            case "lt":
                generateLogicInstruction("JLT");
                break;

            case "and":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and AND it to register, and store result on second topmost
                // element
                decrementARegister(1); // @SP-2
                printWriter.println("M=D&M");

                decreaseSP();
                break;

            case "or":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and AND it to register, and store result on second topmost
                // element
                decrementARegister(1); // @SP-2
                printWriter.println("M=D|M");

                decreaseSP();
                break;

            case "not":
                addressTopmostStackElementAndStoreInDRegister();

                // Store in topmost element negative register, leave SP unaltered
                printWriter.println("M=!D");
                break;

            default:
                throw new IllegalArgumentException(String.format("Unrecognized arithmetic command: %s", command));
        }
    }

    private void decrementARegister(int amount) {
        for (int i = 0; i < amount; i++) {
            printWriter.println("A=A-1");
        }
    }

    private void decreaseSP() {
        printWriter.println("@SP");
        printWriter.println("M=M-1");
    }

    private void addressTopmostStackElementAndStoreInDRegister() {
        printWriter.println("@SP");
        printWriter.println("A=M");
        decrementARegister(1); // @SP-1
        printWriter.println("D=M");
    }

    private void generateLogicInstruction(String jmp) {
        addressTopmostStackElementAndStoreInDRegister();

        // Retrieve second topmost element on stack and subtract it from register, and store result in D
        decrementARegister(1); // @SP-2
        printWriter.println("D=M-D");

        // If D is/is greater than/is lesser than zero, return true (-1) else return false (0)
        printWriter.println(String.format("@LOGIC_%d", labelLogicNumber));
        printWriter.println(String.format("D;%s", jmp));

        // @SP-2
        printWriter.println("@SP");
        printWriter.println("A=M");
        decrementARegister(2);

        printWriter.println("M=0");
        printWriter.println(String.format("@END_LOGIC_%d", labelLogicNumber));
        printWriter.println("0;JMP");
        printWriter.println(String.format("(LOGIC_%d)", labelLogicNumber));

        // @SP-2
        printWriter.println("@SP");
        printWriter.println("A=M");
        decrementARegister(2);

        printWriter.println("M=-1");
        printWriter.println(String.format("(END_LOGIC_%d)", labelLogicNumber));
        decreaseSP();
        labelLogicNumber++;
    }

    /**
     * Writes the assembly code that is the translation of the given command, where command is either C_PUSH or C_POP.
     * @param command the command type, only C_PUSH or C_POP are supported
     * @param segment the segment argument of the command
     * @param index the index argument of the command
     */
    public void writePushPop(VmCommand command, String segment, int index) {
        if (command != VmCommand.C_PUSH && !segment.equals("constant")) {
            throw new UnsupportedOperationException("Only \"push constant N\" commands are supported");
        }

        // push constant index
        // write constant in memory at position SP
        printWriter.println(String.format("@%d", index));
        printWriter.println("D=A");
        printWriter.println("@SP");
        printWriter.println("A=M");
        printWriter.println("M=D");
        increaseSP();
    }

    private void increaseSP() {
        printWriter.println("@SP");
        printWriter.println("M=M+1");
    }

    /**
     * Closes the output file.
     */
    public void close() {
        printWriter.close();
    }

    // More routines will be added to this module in chapter 8
}
