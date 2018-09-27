package no.bibsys;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

public class IOUtilsTest {


    private IOUtils ioUtils=new IOUtils();
    private Path path= Paths.get("policies", "policies/assumeRolePolicy.json");

    @Test
    public void IOUtilsShouldReadAResourceFileAsInputStream() throws IOException {

        InputStream inputSteam = ioUtils.inputStreamFromResources(path);
        int x=inputSteam.read();
        assertThat(x,is(not(equalTo(0))));

    }

    @Test
    public void IOUtilsShouldReadResourceAsListOfStrings() throws IOException {
        List<String> list = ioUtils.linesfromResource(path);
        assertThat(list.size(),is(not(equalTo(0))));
    }


    @Test
    public void IOUtilsShouldReadResourceAsString() throws IOException {
       String content= ioUtils.resourceAsString(path);
       String trimmed=ioUtils.removeMultipleWhiteSpaces(content);
       String expected="{ \"Version\": \"2012-10-17\", \"Statement\": [ { \"Effect\": \"Allow\", \"Principal\": { \"Service\": [ \"lambda.amazonaws.com\" ] }, \"Action\": [ \"sts:AssumeRole\" ] } ]}";
        assertThat(trimmed.length(),is(not(equalTo(0))));
        assertThat(trimmed,is(equalTo(expected)));
    }

}
