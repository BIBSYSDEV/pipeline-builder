package no.bibsys.handler.requests;

import com.google.common.base.Preconditions;

public class RepositoryInfo  {


     private  String owner;
     private  String repository;
     private  String branch;


     public RepositoryInfo(){}

     public RepositoryInfo(String owner,String repository,String branch){
          this.owner=owner;
          this.repository=repository;
          this.branch=branch;
     }


     public final String getOwner(){
          Preconditions.checkNotNull(owner,"\"owner\" field is empty");
          return this.owner;
     }
     public final String getRepository(){
          Preconditions.checkNotNull(repository, "\"repository\" field is empty");
          return this.repository;
     }
     public final  String getBranch(){
          Preconditions.checkNotNull(branch, "\"branch\" field is empty");
          return this.branch;
     }


     public final void setOwner(String owner) {
          this.owner = owner;
     }

     public final void setRepository(String repository) {
          this.repository = repository;
     }

     public final  void setBranch(String branch) {
          this.branch = branch;
     }




}
