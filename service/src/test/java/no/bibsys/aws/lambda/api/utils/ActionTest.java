package no.bibsys.aws.lambda.api.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActionTest {

    private static final String INVALID_ACTION = "invalidOption";

    @Test
    public void toString_action_lowercaseString() {
        for (Action action : Action.values()) {
            assertThat(action.toString(), is(equalTo(action.name().toLowerCase())));
        }
    }

    @Test
    public void fromString_action_lowercaseString() {
        for (Action action : Action.values()) {
            String actionString = action.toString();
            assertThat(Action.fromString(actionString), is(equalTo(action)));
        }
    }

    @Test
    public void fromString_invalidAction_exception() {
        assertThrows(IllegalArgumentException.class, () -> Action.fromString(INVALID_ACTION));
    }
}
