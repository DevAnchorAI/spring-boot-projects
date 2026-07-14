package com.nullcheck.agent;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class NullCheckAgentTest {

    @Test
    public void testConfigInitialization() {
        NullCheckConfig config = new NullCheckConfig();
        assertTrue(config.isCheckMethodCalls());
        assertTrue(config.isCheckFieldAccess());
        assertTrue(config.isCheckArrayAccess());
        assertTrue(config.isCheckMethodChaining());
        assertTrue(config.isExcludeTests());
    }

    @Test
    public void testAddSafeVariable() {
        NullCheckConfig config = new NullCheckConfig();
        config.addSafeVariable("testVar");
        List<String> safeVars = config.getSafeVariables();
        assertTrue(safeVars.contains("testVar"));
    }

    @Test
    public void testAddExcludePattern() {
        NullCheckConfig config = new NullCheckConfig();
        config.addExcludePattern("**/test/**");
        List<String> patterns = config.getExcludePatterns();
        assertTrue(patterns.contains("**/test/**"));
    }

    @Test
    public void testAgentInitialization() {
        NullCheckConfig config = new NullCheckConfig();
        NullCheckAgent agent = new NullCheckAgent(config);
        assertNotNull(agent);
        assertEquals(0, agent.getIssueCount());
    }

    @Test
    public void testIssueCreation() {
        NullCheckIssue issue = new NullCheckIssue(
            "/path/to/file.java",
            42,
            "Test message",
            "String name = user.getName();",
            NullCheckIssue.Severity.WARNING
        );

        assertEquals("/path/to/file.java", issue.getFilePath());
        assertEquals(42, issue.getLineNumber());
        assertEquals("Test message", issue.getMessage());
        assertEquals(NullCheckIssue.Severity.WARNING, issue.getSeverity());
    }

    @Test
    public void testConfigurationToString() {
        NullCheckConfig config = new NullCheckConfig();
        String str = config.toString();
        assertTrue(str.contains("checkMethodCalls"));
        assertTrue(str.contains("checkFieldAccess"));
    }

    @Test
    public void testIssueToString() {
        NullCheckIssue issue = new NullCheckIssue(
            "/path/to/file.java",
            42,
            "Test message",
            "code",
            NullCheckIssue.Severity.INFO
        );
        String str = issue.toString();
        assertTrue(str.contains("NullCheckIssue"));
        assertTrue(str.contains("42"));
    }
}
