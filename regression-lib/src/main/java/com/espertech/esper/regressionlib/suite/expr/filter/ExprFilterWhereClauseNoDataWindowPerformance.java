/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.regressionlib.suite.expr.filter;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class ExprFilterWhereClauseNoDataWindowPerformance {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> executions = new ArrayList<>();
        executions.add(new ExprFilterWhereClauseNoDataWindowPerf());
        return executions;
    }

    private static class ExprFilterWhereClauseNoDataWindowPerf implements RegressionExecution {
        @Override
        public boolean excludeWhenInstrumented() {
            return true;
        }

        public void run(RegressionEnvironment env) {
            // Compares the performance of
            //     select * from SupportBean(theString = 'xyz')
            //  against
            //     select * from SupportBean where theString = 'xyz'
            StringWriter module = new StringWriter();

            for (int i = 0; i < 100; i++) {
                String epl = "@name('s" + i + "') select * from SupportBean where theString = '" + Integer.toString(i) + "';\n";
                module.append(epl);
            }
            EPCompiled compiled = env.compile(module.toString());
            env.deploy(compiled);

            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                SupportBean bean = new SupportBean("NOMATCH", 0);
                env.sendEventBean(bean);
            }
            long end = System.currentTimeMillis();
            long delta = end - start;
            assertTrue("Delta=" + delta, delta < 500);

            env.undeployAll();
        }
    }
}
