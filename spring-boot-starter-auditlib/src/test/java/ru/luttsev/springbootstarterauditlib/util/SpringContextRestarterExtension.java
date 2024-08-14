package ru.luttsev.springbootstarterauditlib.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.test.context.TestContextManager;

public class SpringContextRestarterExtension implements TestInstancePostProcessor, TestWatcher {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        TestContextManager testContextManager = new TestContextManager(testInstance.getClass());
        SpringContextRestarter.getInstance().setTestContextManager(testContextManager);
    }

}
