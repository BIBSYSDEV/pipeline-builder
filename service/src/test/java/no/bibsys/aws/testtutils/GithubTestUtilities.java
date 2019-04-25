package no.bibsys.aws.testtutils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import no.bibsys.aws.secrets.SecretsReader;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;

public class GithubTestUtilities {

    //package-private
    protected static final transient SecretsReader MOCK_SECRETS_READER = () -> "something";
    protected static final String EXPECTED_RESPONSE = "This is the expected response";
    private static final String SUCCESS_REASON_PHRASE = "OK";
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final String PROTOCOL = "http";
    protected static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion(PROTOCOL,
        MAJOR_VERSION, MINOR_VERSION);
    //package-private
    protected static final BasicStatusLine STATUS_LINE_OK = new BasicStatusLine(PROTOCOL_VERSION,
        HttpStatus.SC_OK,
        SUCCESS_REASON_PHRASE);
    private static final String BAD_CREDENTIALS_BODY = "{\"message\":\"Bad credentials\","
        + "\"documentation_url\":\"https://developer.github.com/v3\"}";
    //package-private
    protected static final BasicStatusLine STATUS_LINE_UNAUTHORIZED = new BasicStatusLine(new ProtocolVersion(PROTOCOL,
        MAJOR_VERSION, MINOR_VERSION), HttpStatus.SC_UNAUTHORIZED, BAD_CREDENTIALS_BODY);
    private static final String PATH_NOT_FOUND = "Gituhub path not found";

    protected static final BasicStatusLine STATUS_LINE_NOT_FOUND = new BasicStatusLine(new ProtocolVersion(PROTOCOL,
        MAJOR_VERSION, MINOR_VERSION), HttpStatus.SC_NOT_FOUND, PATH_NOT_FOUND);
    protected final transient BasicHttpEntity simpleResponse;

    public GithubTestUtilities() {
        simpleResponse = new BasicHttpEntity();
        simpleResponse.setContent(new ByteArrayInputStream(EXPECTED_RESPONSE.getBytes(StandardCharsets.UTF_8)));
    }
}
