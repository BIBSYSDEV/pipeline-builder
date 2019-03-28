#!/usr/bin/env python

import subprocess
import sys

# Example: for https://github.com/BIBSYSDEV/authority-registry-infrastructure/
#   repository: authority-registry-infrastructure
#   owner: BIBSYSDEV
#   branch: github branch
#   action: create or delete
#

#
#
DEBUG_ARG = 6
AWS_REGION_ARG = 5
ACTION_ARG = 4
BRANCH_ARG = 3
OWNER_ARG = 1
REPOSITORY_ARG = 2

ARGUMENTS_WITH_DEBUG = 7
ARGUMENTS_WITHOUT_DEBUG = 6

DEBUG_FLAG = "debug"

REPOSITORY_PARAMETER = "repository"
BRANCH_PARAMETER = "branch"
ACTION_PARAMETER = "action"
OWNER_PARAMETER = "owner"
REGION_PARAMETER = "awsRegion"

DEBUG_OPTION = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"

class Script:

  def __init__(self, owner, repository, branch, action, awsRegion, debugOption):
    self._parameters = dict()
    self._parameters[OWNER_PARAMETER] = owner
    self._parameters[REPOSITORY_PARAMETER] = repository
    self._parameters[BRANCH_PARAMETER] = branch
    self._parameters[ACTION_PARAMETER] = action
    self._parameters[REGION_PARAMETER] = awsRegion
    self._debug = DEBUG_FLAG == debugOption

  def _formSystemProperties(self):
    items = self._parameters.items()
    system_properties = [self._dictEntryToSystemProperty(entry) for entry in
                         items]
    return system_properties

  def _executeCommand(self, systemProperties):
    command = ["java", "-classpath", "build/libs/pipeline-fat.jar"]
    if self._debug == True:
      command = command + [DEBUG_OPTION]
    command = command + systemProperties + ["no.bibsys.aws.Application"]

    subprocess.check_call(command)

  def _dictEntryToSystemProperty(self, entry):
    key = entry[0]
    value = entry[1]
    result = "-D{0}={1}".format(key, value)
    return result

  def run(self):
    system_properties = self._formSystemProperties()
    self._executeCommand(system_properties)

  def help(self):
    return \
      """ python ryn.py <owner> <repository> <branch> <action> <awsRegion> [debug]
      - owner:\t\t Github owner
      - repository:\t Github repository
      - branch:\t\t Github branch
      - action:\t\t "create" or "delete"
      - awsRegion:\t Desired AWS region
      - debug (optional)
  """


def main():
  if len(sys.argv) < ARGUMENTS_WITHOUT_DEBUG or len(
      sys.argv) > ARGUMENTS_WITH_DEBUG:
    script = Script(None, None, None, None, None, None)
    print(script.help())
  else:
    owner = sys.argv[OWNER_ARG]
    repository = sys.argv[REPOSITORY_ARG]
    branch = sys.argv[BRANCH_ARG]
    action = sys.argv[ACTION_ARG]
    awsRegion = sys.argv[AWS_REGION_ARG]
    if len(sys.argv) == ARGUMENTS_WITH_DEBUG:
      debugOption = sys.argv[DEBUG_ARG]
      script = Script(owner, repository, branch, action, awsRegion, debugOption)
    else:
      script = Script(owner, repository, branch, action, awsRegion, None)

    script.run()


if __name__ == "__main__":
  main()
