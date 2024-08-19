package ru.luttsev.springbootstarterauditlib.util;

import lombok.Setter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;

public class SpringContextRestarter {

    private static SpringContextRestarter INSTANSE;

    @Setter
    private TestContextManager testContextManager;

    public static SpringContextRestarter getInstance() {
        if (INSTANSE == null) {
            INSTANSE = new SpringContextRestarter();
        }
        return INSTANSE;
    }

    public void restart(Runnable actions) {
        testContextManager.getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
        if (actions != null) {
            actions.run();
        }
        testContextManager.getTestContext().getApplicationContext();
    }

}
