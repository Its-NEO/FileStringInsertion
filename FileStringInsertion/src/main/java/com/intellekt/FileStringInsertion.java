package com.intellekt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import java.io.IOException;

public class FileStringInsertion {
    private String partA, partB;
    private String injectable;
    private File sourceFile;

    private boolean passControl = false;
    private String delimiter = "#";
    private File outputFile;

    private boolean success = true;

    FileStringInsertion(File sourceFile) {
        this.sourceFile = sourceFile;

        outputFile = new File("out\\" + sourceFile.getName());
    }

    FileStringInsertion() {

    }

    public void setPassControl(boolean passControl) {
        this.passControl = passControl;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    private boolean dismantle() {
        Scanner reader = null;
        try {
            reader = new Scanner(sourceFile);
        } catch (FileNotFoundException e) {
            success = false;
            return false;
        }
        if (!reader.hasNext()) {
            System.out.println("Delimiter Not Found.\nTo set a custom delimeter, use .setDelimeter(String delimiter)");
            reader.close();
            success = false;
            return false;
        }
        reader.useDelimiter(delimiter);
        try {
            partA = reader.next();
            partB = reader.next();
        } catch (PatternSyntaxException | NoSuchElementException ignored) {
            System.err.println(
                    "The custom delimeter used to perform this action is invalid. Please use another one or reset it");
            success = false;
        }
        reader.close();
        return true;
    }

    private void write() {
        String finalCode = partA + injectable + partB;

        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                success = false;
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(finalCode);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
            success = false;
        }
    }

    public void inject(String injectable) throws IOException, InterruptedException {
        this.injectable = injectable;
        dismantle();
        write();
        if (passControl) {
            if (!passControl())
                success = false;
        }
    }

    public void inject(String injectable, boolean passControl) throws IOException, InterruptedException {
        this.injectable = injectable;
        dismantle();
        write();
        if (passControl) {
            if (!passControl())
                success = false;
        }
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isSuccessful() {
        return success;
    }

    private boolean passControl() {
        // compile
        Runtime run = Runtime.getRuntime();
        String command = "javac " + outputFile.getPath();
        try {
            Process process = run.exec(command);
            process.waitFor();

            // run
            command = "java " + outputFile.getPath();
            process = run.exec(command);

            // read
            Scanner reader = new Scanner(process.getInputStream());
            while (reader.hasNextLine()) {
                System.out.println(reader.nextLine());
            }
        } catch (IOException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    void reset(){
     passControl = false;
     delimiter = "#";
     success = true;
    }
}