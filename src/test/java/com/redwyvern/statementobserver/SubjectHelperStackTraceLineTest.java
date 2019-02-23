package com.redwyvern.statementobserver;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// Be careful about inserting new code into this test since it relies on testing against line numbers
public class SubjectHelperStackTraceLineTest {

    @Test
    public void shouldReturnCorrectZeroLevelNestedStackTraceLine() {
        assertThat(SubjectHelper.getStackTraceLine(0), equalTo(12));
    }

    @Test
    public void shouldReturnCorrectOneLevelNestedStackTraceLine() {
        assertThat(nestedMethodOneLevel(), equalTo(17));
    }

    @Test
    public void shouldReturnCorrectTwoLevelNestedStackTraceLine() {
        assertThat(nestedMethodTwoLevels(), equalTo(22));
    }

    private int nestedMethodOneLevel() {
        return SubjectHelper.getStackTraceLine(1);
    }

    private int nestedMethodTwoLevels() {
        return nestedMethodTwoLevelsB();
    }

    private int nestedMethodTwoLevelsB() {
        return SubjectHelper.getStackTraceLine(2);
    }
}
