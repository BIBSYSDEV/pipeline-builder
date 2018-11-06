#!/usr/bin/env python

import sys
import subprocess

REPOSITORY = "repository"
BRANCH = "branch"
ACTION = "action"
OWNER = "owner"


class Script:

    def __init__(self, owner, repository, branch, action):
        self._parameters = dict()
        self._parameters[OWNER] = owner
        self._parameters[REPOSITORY] = repository
        self._parameters[BRANCH] = branch
        self._parameters[ACTION] = action

    def _formSystemProperties(self):
        items = self._parameters.items()
        system_properties = [self._dictEntryToSystemProperty(entry) for entry in items]
        return system_properties

    def _executeCommand(self, systemProperties):
        command = ["java", "-classpath", "build/libs/pipeline-fat.jar"]
        command = command + systemProperties + ["no.bibsys.Application"]

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
            """ python ryn.py <owner> <repository> <branch> <action>
            - owner:\t\t Github owner
            - repository:\t Github repository
            - branch:\t\t Github branch
            - action:\t\t "create", "delete", or "update-role"


        """


def main():
    if len(sys.argv) < 5:
        script = Script(None, None, None, None)
        print(script.help())
    else:
        owner = sys.argv[1]
        repository = sys.argv[2]
        branch = sys.argv[3]
        action = sys.argv[4]
        script = Script(owner, repository, branch, action)
        script.run()


if __name__ == "__main__":
    main()
