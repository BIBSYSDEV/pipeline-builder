#!/usr/bin/env bash
#
#

accountId="$(aws sts get-caller-identity --output text --query 'Account')"

echo $accountId

#aws cloudformation deploy --template-file template.yml --stack-name orestis-test \
#--parameter-overides \
# "Branch=autreg-58-openapi-lambda" \
# "ProjectId=autreg-58" \
# "Stage=test" \
# "LambdaTrustRoleArn= arn:aws:iam::933878624978:role/PipelineRole-aut-reg-autreg-60-fix-bug-so-that-tables-get-del"
#
#
#
#  ProjectId:
#    Type: String
#    Description: AWS CodeStar projectID used to associate new resources to team members
#  Stage:
#    Type: String
#    Description: Deployment stage. E.g. test, prod, etc.
#  Branch:
#    Type: String
#  LambdaTrustRoleArn:
#    Type: String
#  CodeBucket:
#    Type: String
