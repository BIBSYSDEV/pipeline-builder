package no.bibsys.cloudformation;


import java.util.Locale;

public enum Stage {

    TEST,FINAL;



    @Override
    public String toString(){
        return this.name().toLowerCase(Locale.getDefault());
    }




}
