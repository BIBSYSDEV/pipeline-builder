package no.bibsys.aws.tools;

import com.google.common.base.Preconditions;
import java.util.Optional;

public class Environment {




    public Optional<String> readEnvOpt(String variableName) {
        return Optional.ofNullable(System.getenv().get(variableName))
             .filter(value->!value.isEmpty());


    }


    public String readEnv(String variableName) {
        String value = System.getenv().get(variableName);
        Preconditions.checkNotNull(value, variableName + " env variable was not found");
        return value;

    }


}
