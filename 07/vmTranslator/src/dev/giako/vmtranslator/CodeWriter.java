package dev.giako.vmtranslator;

import java.io.Closeable;
import java.io.PrintWriter;

/**
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter implements Closeable {
    public static final String UNCONDITIONAL_JMP = "0;JMP";
    private final PrintWriter printWriter;
    private String fileName;
    private int labelLogicNumber = 0;
    private String currentFunction = null;

    /**
     * Opens the input file/stream and gets ready to write into it.
     *
     * @param printWriter a PrintWriter for writing the output file
     */
    public CodeWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    /**
     * Informs the code writer that the translation of a new VM file is started.
     *
     * @param fileName the name of the file to write
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Writes the assembly code that is the translation of the given arithmetic command.
     *
     * @param command the arithmetic command to translate
     */
    public void writeArithmetic(String command) {
        switch (command) {
            case "add":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and add it to register, and store result on second topmost
                // element
                decreaseARegister();
                printWriter.println("M=D+M");

                decreaseSP();
                break;

            case "sub":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and subtract it from register, and store result on second
                // topmost element
                decreaseARegister();
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
                decreaseARegister();
                printWriter.println("M=D&M");

                decreaseSP();
                break;

            case "or":
                addressTopmostStackElementAndStoreInDRegister();

                // Retrieve second topmost element on stack and AND it to register, and store result on second topmost
                // element
                decreaseARegister();
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

    private void decreaseARegister() {
        printWriter.println("A=A-1"); // @SP-2
    }

    private void decreaseSP() {
        printWriter.println("@SP");
        printWriter.println("M=M-1");
    }

    private void addressTopmostStackElementAndStoreInDRegister() {
        addressSPMinusOffset(1);
        printWriter.println("D=M");
    }

    private void generateLogicInstruction(String jmp) {
        addressTopmostStackElementAndStoreInDRegister();

        // Retrieve second topmost element on stack and subtract it from register, and store result in D
        decreaseARegister();
        printWriter.println("D=M-D");

        // If D is/is greater than/is lesser than zero, return true (-1) else return false (0)
        printWriter.println(String.format("@LOGIC_%d", labelLogicNumber));
        printWriter.println(String.format("D;%s", jmp));
        addressSPMinusOffset(2);
        printWriter.println("M=0");
        printWriter.println(String.format("@END_LOGIC_%d", labelLogicNumber));
        printWriter.println(UNCONDITIONAL_JMP);
        printWriter.println(String.format("(LOGIC_%d)", labelLogicNumber));
        addressSPMinusOffset(2);
        printWriter.println("M=-1");
        printWriter.println(String.format("(END_LOGIC_%d)", labelLogicNumber));

        decreaseSP();

        labelLogicNumber++;
    }

    /**
     * Writes the assembly code that is the translation of the given command, where command is either C_PUSH or C_POP.
     *
     * @param command the command type, only C_PUSH or C_POP are supported
     * @param segment the segment argument of the command
     * @param index   the index argument of the command
     */
    public void writePushPop(VmCommand command, String segment, int index) {
        switch (command) {
            case C_PUSH:
                pushValueOfSegmentOntoStack(segment, index);
                break;

            case C_POP:
                popAndStoreInSegment(segment, index);
                break;

            default:
                throw new UnsupportedOperationException("Wrong command passed, " +
                        "this can handle only push or pop commands");
        }

    }

    private void pushValueOfSegmentOntoStack(String segment, int index) {
        loadConstantInA(index);

        // Store in D the value to push
        switch (segment) {
            case "constant":
                printWriter.println("D=A");
                break;

            case "local":
                retrieveValueFromSegmentAndStoreInD("LCL", true);
                break;

            case "argument":
                retrieveValueFromSegmentAndStoreInD("ARG", true);
                break;

            case "this":
                retrieveValueFromSegmentAndStoreInD("THIS", true);
                break;

            case "that":
                retrieveValueFromSegmentAndStoreInD("THAT", true);
                break;

            case "temp":
                retrieveValueFromSegmentAndStoreInD("R5", false);
                break;

            case "pointer":
                retrieveValueFromSegmentAndStoreInD("R3", false);
                break;

            case "static":
                printWriter.println(String.format("@%s.%d", fileName, index));
                printWriter.println("D=M");
                break;

            default:
                throw new UnsupportedOperationException(String.format("Unsupported segment: %s", segment));
        }

        addressSPMinusOffset(0);
        printWriter.println("M=D");
        increaseSP();
    }

    private void retrieveValueFromSegmentAndStoreInD(String segmentPointer, boolean dereferenceSegmentPointer) {
        printWriter.println("D=A");
        addressSymbol(segmentPointer);

        if (dereferenceSegmentPointer) {
            printWriter.println("A=D+M");
        }
        else {
            printWriter.println("A=D+A");
        }

        printWriter.println("D=M");
    }

    private void popAndStoreInSegment(String segment, int index) {
        calculateAddressAndStoreInR13(segment, index);
        addressSPMinusOffset(1);
        printWriter.println("D=M");
        printWriter.println("@R13");
        printWriter.println("A=M");
        printWriter.println("M=D");
        decreaseSP();
    }

    private void addressSPMinusOffset(int offset) {
        printWriter.println("@SP");
        printWriter.println("A=M");

        for (int i = 0; i < offset; i++) {
            decreaseARegister();
        }
    }

    private void calculateAddressAndStoreInR13(String segment, int index) {

        switch (segment) {
            case "constant":
                throw new UnsupportedOperationException("Cannot pop on constant segment");

            case "local":
                calculateSegmentAndIndexAddress("LCL", index, true);
                break;

            case "argument":
                calculateSegmentAndIndexAddress("ARG", index, true);
                break;

            case "this":
                calculateSegmentAndIndexAddress("THIS", index, true);
                break;

            case "that":
                calculateSegmentAndIndexAddress("THAT", index, true);
                break;

            case "temp":
                calculateSegmentAndIndexAddress("R5", index, false);
                break;

            case "pointer":
                calculateSegmentAndIndexAddress("R3", index, false);
                break;

            case "static":
                printWriter.println(String.format("@%s.%d", fileName, index));
                printWriter.println("D=A");
                break;

            default:
                throw new UnsupportedOperationException(String.format("Unsupported segment: %s", segment));
        }

        printWriter.println("@R13");
        printWriter.println("M=D");
    }

    private void calculateSegmentAndIndexAddress(String virtualRegister, int index, boolean dereferenceRegister) {
        loadConstantInA(index);
        printWriter.println("D=A");
        addressSymbol(virtualRegister);

        if (dereferenceRegister) {
            printWriter.println("A=M");
        }

        printWriter.println("D=D+A");
    }

    private void addressSymbol(String symbol) {
        printWriter.println(String.format("@%s", symbol));
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

    /**
     * Writes assembly code that effects the VM initialization, also called bootstrap code. This code must be placed at
     * the beginning of the output file.
     */
    public void writeInit() {
        // TODO implement assembler
    }

    /**
     * Writes assembly code that effects the label command.
     * @param label the label string
     */
    public void writeLabel(String label) {
        printWriter.println(String.format("(%s$%s)", currentFunction, label));
    }

    /**
     * Writes assembly code that effects the goto command.
     * @param label the label string to jump to
     */
    public void writeGoto(String label) {
        printWriter.println(String.format("@%s$%s", currentFunction, label));
        printWriter.println(UNCONDITIONAL_JMP);
    }

    /**
     * Writes assembly code that effects the if-goto command.
     * @param label the label string to jump to
     */
    public void writeIf(String label) {
        // pop value from the stack
        addressSPMinusOffset(1);
        printWriter.println("D=M");
        decreaseSP();

        printWriter.println(String.format("@%s$%s", currentFunction, label));
        printWriter.println("D;JNE");
    }

    /**
     * Writes assembly code that effects the call command.
     * @param functionName the name of the function to call
     * @param numArgs the number of arguments pushed to the stack
     */
    public void writeCall(String functionName, int numArgs) {
        // push return-address
        String returnAddressSymbol = String.format("%s_RETURN_ADDRESS", functionName);
        pushPointerToStack(returnAddressSymbol);
        pushPointerToStack("LCL");
        pushPointerToStack("ARG");
        pushPointerToStack("THIS");
        pushPointerToStack("THAT");
        // ARG = SP - n - 5
        loadSPInD();
        loadConstantInA(numArgs);
        printWriter.println("D=D-A");
        loadConstantInA(5);
        printWriter.println("D=D-A");
        printWriter.println("M=D");
        // LCL = SP
        loadSPInD();
        printWriter.println("@LCL");
        printWriter.println("M=D");
        // goto f
        addressSymbol(functionName);
        printWriter.println(UNCONDITIONAL_JMP);
        // (return-address)
        printWriter.println(String.format("(%s)", returnAddressSymbol));
        currentFunction = functionName;
    }

    private void loadSPInD() {
        printWriter.println("@SP");
        printWriter.println("D=A");
    }

    private void loadConstantInA(int constant) {
        printWriter.println(String.format("@%d", constant));
    }

    private void pushPointerToStack(String symbol) {
        addressSymbol(symbol);
        printWriter.println("D=A");
        addressSPMinusOffset(0);
        printWriter.println("M=D");
        increaseSP();
    }

    /**
     * Writes assembly code that effects the return command.
     */
    public void writeReturn() {
        // FRAME = LCL
        printWriter.println("@LCL");
        printWriter.println("D=M");
        printWriter.println("@R13"); // FRAME
        printWriter.println("M=D");
        // RET = *(FRAME-5)
        loadConstantInA(5);
        printWriter.println("A=D-A");
        printWriter.println("D=M");
        printWriter.println("@R14"); // RET
        printWriter.println("M=D");
        // *ARG = pop()
        addressSPMinusOffset(1);
        printWriter.println("D=M");
        printWriter.println("@ARG");
        printWriter.println("A=M");
        printWriter.println("M=D");
        decreaseSP();
        // SP = ARG + 1
        printWriter.println("@ARG");
        printWriter.println("D=M+1");
        printWriter.println("@SP");
        printWriter.println("M=D");
        // THAT = *(FRAME - 1)
        dereferencePreviousElementInFrame("THAT");
        // THIS = *(FRAME - 2)
        dereferencePreviousElementInFrame("THIS");
        // ARG = *(FRAME - 3)
        dereferencePreviousElementInFrame("ARG");
        // LCL = *(FRAME - 4)
        dereferencePreviousElementInFrame("LCL");
        // goto RET
        printWriter.println("@R14");
        printWriter.println("A=M");
        printWriter.println(UNCONDITIONAL_JMP);
    }

    private void dereferencePreviousElementInFrame(String assignToSymbol) {
        printWriter.println("@R13");
        printWriter.println("AM=M-1");
        printWriter.println("D=M");
        addressSymbol(assignToSymbol);
        printWriter.println("M=D");
    }

    /**
     * Writes assembly code that effects the function command.
     * @param functionName the name of the function to declare
     * @param numLocals the number of local variables to instantiate
     */
    public void writeFunction(String functionName, int numLocals) {
        printWriter.println(String.format("(%s)", functionName));

        for (int i = 0; i < numLocals; i++) {
            addressSPMinusOffset(0);
            printWriter.println("M=0");
            increaseSP();
        }
    }
}
