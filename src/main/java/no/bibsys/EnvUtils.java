package no.bibsys;

public interface EnvUtils {


    default String getEnvVariable(String variableName){
        return System.getenv().get(variableName);
    }

}
