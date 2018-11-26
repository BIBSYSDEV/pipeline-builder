package no.bibsys.aws.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import no.bibsys.aws.tools.Environment;
import org.junit.Test;

public class EnvironmentTest {


    Environment environment=new Environment();



    @Test(expected = NullPointerException.class)
    public void getEnv_nonExistingEnvVariable_throwsException(){
        String value=environment.readEnv("something_here");

    }


    @Test
    public void getEnvOpt_nonExistingEnvVariable_emptyOptional(){
        Optional<String> value = environment.readEnvOpt("something_here");
        assertThat(value,is(equalTo(Optional.empty())));
    }


    @Test
    public void getEnvOpt_existingVariable_envVariableValue(){
        Map<String, String> envVariable = randoEnvVariable();

        String key=envVariable.keySet().iterator().next();
        String expectedValue=envVariable.get(key);

        Optional<String> actualValue=environment.readEnvOpt(key);

        assertTrue(actualValue.isPresent());
        assertThat(actualValue.get(),is(equalTo(expectedValue)));
    }

    private Map<String, String> randoEnvVariable() {
        Map<String, String> map = System.getenv();
        assertFalse(map.isEmpty());

        String value= null;
        String key= null;
        Iterator<String> keys=map.keySet().iterator();
        while (value==null){
            key=keys.next();
            value=map.get(key);
        }
        Map<String,String> envVariable=Collections.singletonMap(key,value);
        return envVariable;
    }


}
