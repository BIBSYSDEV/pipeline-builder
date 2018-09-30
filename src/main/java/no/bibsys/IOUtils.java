package no.bibsys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {



    public InputStream inputStreamFromResources(Path path){
        String pathString=path.toString();
        return this.getClass().getClassLoader().getResourceAsStream(pathString);
    }


    public List<String> linesfromResource(Path path) throws IOException {
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStreamFromResources(path)));
        List<String> lines=new ArrayList<>();
        String line=reader.readLine();
        while(line!=null){
            lines.add(line);
            line=reader.readLine();
        }
        return lines;
    }

    public String resourceAsString(Path path) throws IOException {
        List<String> lines=linesfromResource(path);
        String result=String.join("\n",lines);
        return result;
    }


    public String removeMultipleWhiteSpaces(String input){
        String buffer=input.trim();
        String result=buffer.replaceAll("\\s\\s"," ");
        while(!result.equals(buffer)){
            buffer=result;
            result=buffer.replaceAll("\\s\\s"," ");
        }
        return result;
    }


}
