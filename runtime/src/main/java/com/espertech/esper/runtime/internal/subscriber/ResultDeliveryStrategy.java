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
package com.espertech.esper.runtime.internal.subscriber;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.collection.UniformPair;

/**
 * Strategy for use with statement-result-service to dispatch to a statement's subscriber
 * via method invocations.
 */
public interface ResultDeliveryStrategy {
    /**
     * Execute the dispatch.
     *
     * @param result is the insert and remove stream to indicate
     */
    public void execute(UniformPair<EventBean[]> result);
}
