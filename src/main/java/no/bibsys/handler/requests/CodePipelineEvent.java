package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.utils.JsonUtils;


public class CodePipelineEvent {

    private final String id;

    public CodePipelineEvent(String id) {
        this.id = id;
    }


    public static CodePipelineEvent create(String eventJsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(eventJsonString);
        String id = root.get("id").asText();
        return new CodePipelineEvent(id);
    }


    public String getId() {
        return id;
    }


}


/*{
  "CodePipeline.job": {
    "id": "a0a4b321-beb6-4da6-a595-dab82e23de40", "accountId": "933878624978", "data": {
      "actionConfiguration": {
        "configuration": {"FunctionName": "aut-reg-inf-autreg-58-openapi-lambdab-init-function"}
      }, "inputArtifacts"  : [], "outputArtifacts": [], "artifactCredentials": {
        "sessionToken": "AgoGb3JpZ2luEJH//////////wEaCWV1LXdlc3QtMSKAAowlR43/OTrmeMPjynEV9Ime7Li/2C2twoIlBpuI7II2L6joI4QfeIJ/59QssJnxwbo3YeOALuoUuZq3MfmzLbJ3To5PuqKAobeQL8CF1h8VV9PzeKTTTKcdVGFSRChskivA9oJiUJJ5xscr2OgWrVAbhBlDOX+RKeD77VLgSbsvnzSTdIH4AIBIrIS5ETOaSNVXNX9gDXb7d1hq7LilQmHwROBct2LaU8+QdvaMBH+kLhMZQZPnOnVe5DkqphHu9rodnxisRQEVBJS/w43NjZY62Hw844ewLKn6dNg15CILmSV8xeHqfd3aK0a0J1hdCeTPvhXDSptLFG9r2UBztvIq6wMIFhAAGgw5MzM4Nzg2MjQ5NzgiDPVAK/mVDsjA2KkSMirIAyZrawpzfY6eyxJm830KQt8g8boDYDzOYaG8mOSmvGhTuOWCrZThy3jmbAsfu4HtuSBegDw6R+0lrw4F4WyJylNQAnDEmj6BVBdjOaTHJ3/9VCtr+4fTAsvChkUws2NU56zF91HCzT1JBTQxtyahdswEsyMY0mmmfvi4wrvAJHD3H4ZPyIXhc9vtOTYJffd6SifdbCryQxElrolSUpuWx0oY+khkRDqHrTYUdSkOCOSUiNflB6XJdg4GpJswhPI7HuunWXXqo9ge+OEdZ8L7/5of8YRVmYqn58PV3Ym0W/fpN/CLvmtmpJ6rrAm3k2pb27kgdElMKuZyYD46Pokvq+f8T8CUaIR0fwGx2BGuAI6W4PdoNIC84ASmcaDNXRJeITzj35bLz5iSmQiNEHxEK5n1eZxOm+2K5UjxgJwULzecHf6MTBLuOdpbJKMFhZBfkJ/4GgHDqADKJ+Tz2D0GaIZE3j8AXts8ybhNigPq6sEmUBAKOhaZyRTMGdztpjODiG3Y+8lmqFRZsPWjQxFDZFxJ9ciJpgcNhpHyGEZ/yA0A/QnmNkbucdRM8y/sbAxU5NFgPONVx6qwpeWuCaCLba25UF6TPp6JlzDxvYvfBQ==",
        "secretAccessKey": "S+t/iMiDhOdm3LRR2yxpJpYcc+RRvpfL0AUskGvE",
        "accessKeyId": "ASIA5S34A6LJMKX7VH74"
      }
    }
  }
}*/