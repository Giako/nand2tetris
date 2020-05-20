package dev.giako;

import java.io.*;

public class Assembler {
    private static final String OUTPUT_FILENAME_EXT = ".hack";
    private Parser parser;
    private final SymbolTable symbolTable = new SymbolTable();
    private int nextFreeRamAddress = 16;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Provide at least one file to process");
            System.out.println("Example: java -jar Assembler.jar Prog.asm");
            return;
        }

        var assembler = new Assembler();
        var asmFilename = args[0];
        var outputFilename = asmFilename.replaceAll("\\.asm$", "").concat(OUTPUT_FILENAME_EXT);
        assembler.firstPass(asmFilename);
        assembler.secondPass(asmFilename, outputFilename);
    }

    private void firstPass(String inputFilename) {
        addDefaultSymbols();
        var programCounter = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
            parser = new Parser(br);

            while (parser.hasMoreCommands()) {
                switch (parser.getCommandType()) {
                    case C_COMMAND:
                    case A_COMMAND:
                        programCounter++;
                        break;

                    case L_COMMAND:
                        symbolTable.addEntry(parser.getSymbol(), programCounter);
                        break;

                    default:
                        throw new UnsupportedOperationException("Instruction command type unknown");
                }

                parser.advance();
            }
        } catch (IOException e) {
            System.err.println(String.format("Cannot open file: %s", inputFilename));
            e.printStackTrace();
        }
    }

    private void addDefaultSymbols() {
        symbolTable.addEntry("SP", 0x0);
        symbolTable.addEntry("LCL", 0x1);
        symbolTable.addEntry("ARG", 0x2);
        symbolTable.addEntry("THIS", 0x3);
        symbolTable.addEntry("THAT", 0x4);
        symbolTable.addEntry("R0", 0x0);
        symbolTable.addEntry("R1", 0x1);
        symbolTable.addEntry("R2", 0x2);
        symbolTable.addEntry("R3", 0x3);
        symbolTable.addEntry("R4", 0x4);
        symbolTable.addEntry("R5", 0x5);
        symbolTable.addEntry("R6", 0x6);
        symbolTable.addEntry("R7", 0x7);
        symbolTable.addEntry("R8", 0x8);
        symbolTable.addEntry("R9", 0x9);
        symbolTable.addEntry("R10", 0xa);
        symbolTable.addEntry("R11", 0xb);
        symbolTable.addEntry("R12", 0xc);
        symbolTable.addEntry("R13", 0xd);
        symbolTable.addEntry("R14", 0xe);
        symbolTable.addEntry("R15", 0xf);
        symbolTable.addEntry("SCREEN", 0x4000);
        symbolTable.addEntry("KBD", 0x6000);
    }

    private void secondPass(String inputFilename, String outputFilename) {
        try (
                BufferedReader br = new BufferedReader(new FileReader(inputFilename));
                PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))
        ) {
            parser = new Parser(br);

            while (parser.hasMoreCommands()) {

                switch (parser.getCommandType()) {
                    case C_COMMAND:
                        writer.println(generateAsmCCommand());
                        break;

                    case A_COMMAND:
                        writer.println(generateAsmACommand());
                        break;

                    case L_COMMAND:
                        // No generation for L-commands
                        break;

                    default:
                        throw new UnsupportedOperationException("Instruction command type unknown");
                }

                parser.advance();
            }
        } catch (IOException e) {
            System.err.println(String.format("Cannot open file: %s", inputFilename));
            e.printStackTrace();
        }
    }

    private String generateAsmACommand() {
        var symbol = retrieveSymbol();

        return String.format("0%15s", Integer.toBinaryString(symbol)).replace(' ', '0');
    }

    private int retrieveSymbol() {
        var symbol = parser.getSymbol();

        try {
            // if it's a valid integer, return that
            return Integer.parseInt(symbol);
        } catch (NumberFormatException e) {
            // Not an integer, search in symbol table or add a new symbol
            if (Character.isDigit(symbol.charAt(0))) {
                throw new IllegalArgumentException("Symbol cannot begin with a number");
            }

            if (symbolTable.symbol(symbol)) {
                return symbolTable.getAddress(symbol);
            }

            symbolTable.addEntry(symbol, nextFreeRamAddress);
            return nextFreeRamAddress++;
        }
    }

    private String generateAsmCCommand() {
        var comp = Code.comp(parser.getComp());
        var dest = Code.dest(parser.getDest());
        var jump = Code.jump(parser.getJump());

        return String.format("111%7s%3s%3s", Integer.toBinaryString(comp), Integer.toBinaryString(dest),
                Integer.toBinaryString(jump)).
                replace(' ', '0');
    }
}
