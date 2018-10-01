package no.bibsys.role;

import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.IOUtils;

public class PipelineRole {


  private final String s3BucketName;
  private final String branch;

  public PipelineRole(String projectName,String branch){
    s3BucketName=projectName;
    this.branch=branch;
  }

  private RoleHelper helper=new RoleHelper();
  private IOUtils ioUtils=new IOUtils();

  public Role createPipelineRole(String roleName) throws IOException {
    helper.deleteRole(roleName);
    helper.createEmptyRole(roleName);
    helper.attachPolicies( roleName,buildPipelinePolicies());
    PutRolePolicyRequest inlinePolicy = bucketAccessInlinePolicy(roleName);
    helper.putRolePolicy(inlinePolicy);
    Role role = helper.getRole(roleName).get();
    return role;
  }


  private List<String> buildPipelinePolicies() {
    List<String> policies = new ArrayList<>();
    policies.add("arn:aws:iam::aws:policy/AWSCodePipelineFullAccess");
    policies.add("arn:aws:iam::aws:policy/AWSCodeCommitFullAccess");
    policies.add("arn:aws:iam::aws:policy/AWSCodeBuildAdminAccess");
//        policies.add("arn:aws:iam::aws:policy/AmazonS3FullAccess");
    return policies;
  }


  private PutRolePolicyRequest bucketAccessInlinePolicy(String roleName) throws IOException {

    String inlinePolicyName = roleName + "_bucket_access";
    String accessToBucket = ioUtils
        .resourceAsString(Paths.get("policies", "accessToBucket.json"));
    accessToBucket.replaceAll("\\[BUCKET_NAME\\]",s3BucketName);

    return new PutRolePolicyRequest()
        .withPolicyDocument(accessToBucket)
        .withRoleName(roleName)
        .withPolicyName(inlinePolicyName);
  }


}
