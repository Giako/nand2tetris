package dev.giako;

import java.io.*;

public class Assembler {
    private static final String OUTPUT_FILENAME_EXT = ".hack";
    private Parser parser;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Provide at least one file to process");
            //noinspection SpellCheckingInspection
            System.out.println("Example: java -jar Assembler.jar Prog.asm");
            return;
        }

        var assembler = new Assembler();
        var asmFilename = args[0];
        var outputFilename = asmFilename.replaceAll("\\.asm$", "").concat(OUTPUT_FILENAME_EXT);
        assembler.run(asmFilename, outputFilename);
    }

    private void run(String inputFilename, String outputFilename) {

        try (
                BufferedReader br = new BufferedReader(new FileReader(inputFilename));
                PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))
        ) {
            parser = new Parser(br);

            while (parser.hasMoreCommands()) {
                String outputLine;

                switch (parser.getCommandType()) {
                    case C_COMMAND:
                        outputLine = generateAsmCCommand();
                        break;

                    case A_COMMAND:
                        outputLine = generateAsmACommand();
                        break;

                    case L_COMMAND:
                        throw new UnsupportedOperationException("L commands still not implemented");

                    default:
                        throw new UnsupportedOperationException("Instruction command type unknown");
                }

                writer.println(outputLine);
                parser.advance();
            }
        } catch (IOException e) {
            System.err.println(String.format("Cannot open file: %s", inputFilename));
            e.printStackTrace();
        }
    }

    private String generateAsmACommand() {
        var symbol = Integer.parseInt(parser.getSymbol());

        return String.format("0%15s", Integer.toBinaryString(symbol)).replace(' ', '0');
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
