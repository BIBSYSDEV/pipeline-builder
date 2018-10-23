package no.bibsys.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IoUtils {


    public InputStream inputStreamFromResources(Path path) {
        String pathString = path.toString();
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
    }


    public String streamToString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        String output = String.join("\n", lines);
        return output;
    }


    public String fileAsString(Path path) throws IOException {
        InputStream fileInputStream = Files.newInputStream(path);
        return streamToString(fileInputStream);
    }


    public List<String> linesfromResource(Path path) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStreamFromResources(path)));
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        return lines;
    }

    public String resourceAsString(Path path) throws IOException {
        List<String> lines = linesfromResource(path);
        String result = String.join("\n", lines);
        return result;
    }


    public String removeMultipleWhiteSpaces(String input) {
        String buffer = input.trim();
        String result = buffer.replaceAll("\\s\\s", " ");
        while (!result.equals(buffer)) {
            buffer = result;
            result = buffer.replaceAll("\\s\\s", " ");
        }
        return result;
    }


}
