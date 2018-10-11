package no.bibsys.utils;

import java.util.Optional;

public abstract class EnvUtils {


    protected String readEnv(String variableName){
        Optional<String> value = Optional.ofNullable(System.getenv().get(variableName));
        if(value.isPresent())
            return value.get();
        else{
            throw new IllegalStateException(String.format("Env variable %s is missing",variableName));
        }

    }

}
