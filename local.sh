#!/usr/bin/env bash


sam local invoke Init --no-event -d 5005 --parameter-overrides \
'ParameterKey=ProjectId,ParameterValue=aut-reg-inf ParameterKey=Stage,ParameterValue=final ParameterKey=Branch,ParameterValue=hotfixes'


#ProjectId:
#    Type: String
#  Stage:
#    Type: String
#    Description: Deployment stage. E.g. test, prod, etc.
#  Branch:
#    Type: String
#  CodeBucket:
#    Type: String
#  InitFunctionName:
#    Type: String
#  DestroyFunctionName:
#    Type:  String