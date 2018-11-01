# Building Pipelines

##Build
 ` ./build.sh ` 
 
## Run 

 `python run.py <owner> <repository> <branch> <action>` 
 
 where 
 
  * `owner` is the Github owner.
  * `repository` is the Github repository name.
  * `branch` is the branch to be build
  * `action` is one of `create`, `delete`, `update-role`
  
  For example to create a stack for  the branch `master` of the Github repository `https://github.com/BIBSYSDEV/authority-registry`
  we will run the following command:
  
  `python run.py BIBSYSDEV authority-registry master create`
  
  To delete the stack we will run :
  
  `python run.py BIBSYSDEV authority-registry master delete`
  
  To update the Role that is executing the code we will run the command:
  
  `python run.py BIBSYSDEV authority-registry master update-role`