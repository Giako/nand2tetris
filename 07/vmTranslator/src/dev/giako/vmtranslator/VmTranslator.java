package dev.giako.vmtranslator;

import java.io.*;

public class VmTranslator {

    public static final String INPUT_FILENAME_EXT = ".vm";
    private static final String OUTPUT_FILENAME_EXT = ".asm";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Provide at least one file or directory to process");
            System.out.println("Example: java -jar VmTranslator.jar source");
            System.out.println("         where source is either a file name of the form Xxx.vm or a directory name");
            System.out.println("         containing one or more .vm files.");
            return;
        }

        var vmTranslator = new VmTranslator();
        var fileArgument = new File(args[0]);
        var outputFilename = args[0].replaceAll("\\.vm$", "").concat(OUTPUT_FILENAME_EXT);

        // If a directory was provided, extract its contents
        File[] files;

        if (fileArgument.exists() && fileArgument.isDirectory()) {
            files = fileArgument.listFiles((dir, name) -> name.endsWith(INPUT_FILENAME_EXT));
            outputFilename = String.format("%s/%s", fileArgument.getAbsolutePath(), outputFilename);
        }
        else if (fileArgument.exists() && fileArgument.isFile()) {
            files = new File[1];
            files[0] = fileArgument;
        }
        else {
            throw new IllegalArgumentException("Provided filename not exists");
        }

        assert files != null;
        vmTranslator.translate(files, outputFilename);
    }

    private void translate(File[] files, String outputFilename) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(outputFilename));
             CodeWriter codeWriter = new CodeWriter(printWriter)) {
            for (File file :
                    files) {
                var fileName = file.getName();
                System.out.println(String.format("Parsing file: %s", fileName));
                codeWriter.setFileName(fileName);
                parseFile(file, codeWriter);
                System.out.println("Parsing done.");
            }
        } catch (IOException e) {
            System.err.println("Error in parsing file");
            e.printStackTrace();
        }

    }

    private void parseFile(File file, CodeWriter codeWriter) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Parser parser = new Parser(br);

            while (parser.hasMoreCommands()) {
                switch (parser.getCommandType()) {
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.getArg1());
                        break;

                    case C_PUSH:
                        codeWriter.writePushPop(VmCommand.C_PUSH, parser.getArg1(), parser.getArg2());
                        break;

                    case C_POP:
                        codeWriter.writePushPop(VmCommand.C_POP, parser.getArg1(), parser.getArg2());
                        break;

                    default:
                        throw new UnsupportedOperationException("Only arithmetic, push, pop commands are supported");
                }

                parser.advance();
            }
        }
    }
}
