package nl.weeaboo.vn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.vn.impl.Context;
import nl.weeaboo.vn.impl.ContextArgs;
import nl.weeaboo.vn.impl.ContextManager;
import nl.weeaboo.vn.script.IScriptContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContextTest extends AbstractEntityTest {

    private ContextManager contextManager;
    private Context alpha;
    private Context beta;

    @Before
    @Override
    public void init() {
        super.init();

        IScreen screen = TestUtil.newScreen(pr, scene);
        IScriptContext scriptContext = TestUtil.newScriptContext();

        ContextArgs contextArgs = new ContextArgs();
        contextArgs.scene = scene;
        contextArgs.drawablePart = pr.drawable;
        contextArgs.screen = screen;
        contextArgs.scriptContext = scriptContext;

        contextManager = new ContextManager(contextArgs);
        alpha = contextManager.createContext();
        beta = contextManager.createContext();
    }

    @Test
    public void changeActiveContext() {
        assertActiveContexts();

        contextManager.setContextActive(alpha, true);
        assertActiveContexts(alpha);

        contextManager.setContextActive(alpha, false);
        assertActiveContexts();

        contextManager.setContextActive(alpha, true);
        contextManager.setContextActive(beta, true);
        assertActiveContexts(alpha, beta);
    }

    private void assertActiveContexts(IContext... actives) {
        Set<IContext> activesSet = new HashSet<IContext>();
        Collections.addAll(activesSet, actives);

        for (IContext context : contextManager.getContexts()) {
            Assert.assertEquals(activesSet.contains(context), context.isActive());
        }

        // Ensure getActiveContexts() contains the expected elements and no others
        Assert.assertTrue(contextManager.getActiveContexts().containsAll(activesSet));
        Assert.assertTrue(activesSet.containsAll(contextManager.getActiveContexts()));
    }

}
