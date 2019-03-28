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
  if len(sys.argv) < 6 or len(sys.argv) > 7:
    script = Script(None, None, None, None, None, None)
    print(script.help())
  else:
    owner = sys.argv[1]
    repository = sys.argv[2]
    branch = sys.argv[3]
    action = sys.argv[4]
    awsRegion = sys.argv[5]
    if len(sys.argv) == 7:
      debugOption = sys.argv[6]
      script = Script(owner, repository, branch, action, awsRegion, debugOption)
    else:
      script = Script(owner, repository, branch, action, awsRegion, None)

    script.run()


if __name__ == "__main__":
  main()
