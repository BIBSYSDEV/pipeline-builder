package no.bibsys.utils;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "master";
    private String repoName = "authority-registry";
    private String repoOwner = "BIBSYSDEV";

    private final IoUtils ioUtils=new IoUtils();

    Environment environment= new Environment(){
        @Override
        public Optional<String> readEnvOpt(String variableName) {
            return Optional.of("env-variable");
        }

    };

    @Test
    @Ignore
    public void createStacks() throws IOException {
        Application application = new Application(new Environment());

        application.withBranch(branchName)
            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .createStacks();

    }


    @Test
    @Ignore
    public void deleteStacks() throws IOException {
        Application application = new Application(new Environment());
        application.withBranch(branchName)

            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .wipeStacks();

    }




    @Test
    @Ignore
    public void createRole() throws IOException {
        AmazonIdentityManagement iam= AmazonIdentityManagementClientBuilder.defaultClient();
        PipelineStackConfiguration pipelineStackConfiguration=new PipelineStackConfiguration(branchName,
            repoName,
            repoOwner,
            environment);

        String assumePolicy = ioUtils.fileAsString(Paths.get("assumePolicy.json"));
        String validJson=JsonUtils.removeComments(assumePolicy);
        CreateRoleRequest createRoleRequest=new CreateRoleRequest();
        createRoleRequest.withAssumeRolePolicyDocument(validJson)
            .withRoleName(pipelineStackConfiguration.getPipelineConfiguration().getLambdaTrustRolename())
            .withPath("/");
        iam.createRole(createRoleRequest);

        iam.listRoles().getRoles().stream().map(role->
            new DeleteRoleRequest().withRoleName(role.getRoleName()))
            .forEach(req->iam.deleteRole(req));




    }


    @Test
    @Ignore
    public void checkLambdaTrustRole() throws IOException {
        String auth="token 40a5b58038a4d54cf9975393e244e75506b6edbb";
        String url=String.format(
            "https://api.github.com/repos/%s/%s/contents/template.yml?ref=%s&path=template.yml",
        repoOwner,repoName,branchName);

        CloseableHttpClient client= HttpClients.createDefault();
        HttpGet get=new HttpGet(url);
        get.setHeader(new BasicHeader("Authentication", auth));
        get.setHeader(new BasicHeader("Accept","application/vnd.github.v3+json"));
        CloseableHttpResponse response = client.execute(get);


        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        IoUtils ioUtils=new IoUtils();
        String responseString = ioUtils.streamToString(stream);
        ObjectMapper mapper=new ObjectMapper();
        JsonNode node = mapper.readTree(responseString);
        String downloadUrl=node.get("download_url").asText();
        response.close();
         get=new HttpGet(downloadUrl);
        InputStream contenteStream = client.execute(get).getEntity().getContent();
        String fileContent=ioUtils.streamToString(contenteStream);
        System.out.println(fileContent);




    }








}
