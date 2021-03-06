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
package com.espertech.esper.common.internal.epl.resultset.grouped;

import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.output.polled.OutputConditionPolled;
import com.espertech.esper.common.internal.epl.output.polled.OutputConditionPolledFactory;

import java.util.HashMap;
import java.util.Map;

public class ResultSetProcessorGroupedOutputFirstHelperImpl implements ResultSetProcessorGroupedOutputFirstHelper {
    private final Map<Object, OutputConditionPolled> outputState = new HashMap<>();

    public void remove(Object key) {
        outputState.remove(key);
    }

    public OutputConditionPolled getOrAllocate(Object mk, ExprEvaluatorContext exprEvaluatorContext, OutputConditionPolledFactory factory) {
        OutputConditionPolled outputStateGroup = outputState.get(mk);
        if (outputStateGroup == null) {
            outputStateGroup = factory.makeNew(exprEvaluatorContext);
            outputState.put(mk, outputStateGroup);
        }
        return outputStateGroup;
    }

    public OutputConditionPolled get(Object mk) {
        return outputState.get(mk);
    }

    public void put(Object mk, OutputConditionPolled outputStateGroup) {
        outputState.put(mk, outputStateGroup);
    }

    public void destroy() {
        // no action required
    }
}
