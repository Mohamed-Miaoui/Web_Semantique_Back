package com.example.jardin_urbain_ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileTool {
    public FileTool() {
    }

    public static String getContents(File file) {
        StringBuilder contents = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = input.readLine()) != null) {
                contents.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents.toString();
    }
}