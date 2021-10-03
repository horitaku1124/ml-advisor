package com.github.horitaku1124.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CSVReader implements Closeable {
    private final BufferedReader fileReader;
    private int header;

    public CSVReader(String path, int header) throws IOException {
        fileReader = Files.newBufferedReader(Paths.get(path));
        this.header = header;
    }

    public String[] read() throws IOException {
        String line = fileReader.readLine();
        if (line == null) {
            return null;
        }
        return line.split(",");
    }

    public <R> List<R> readClassAll(Function<String, R> function) throws IOException {
        List<R> data = new ArrayList<>();
        int num = 0;
        while(true) {
            String line = fileReader.readLine();
            if (line == null) {
                break;
            }
            if (num != header - 1) {
                data.add(function.apply(line));
            }
            num++;
        }
        return data;
    }

    @Override
    public void close() throws IOException {
        fileReader.close();
    }
}
