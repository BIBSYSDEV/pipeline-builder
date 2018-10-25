package no.bibsys.utils;

import java.util.Optional;

public class MockEnvironment extends Environment {


    @Override
    public Optional<String> readEnvOpt(String variableName) {
        return Optional.of(variableName);
    }


}
