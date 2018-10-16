package no.bibsys.utils;

import java.util.Optional;

public class Environment {

    public Optional<String> readEnvOpt(String variableName){
        return Optional.ofNullable(System.getenv().get(variableName));


    }



}
