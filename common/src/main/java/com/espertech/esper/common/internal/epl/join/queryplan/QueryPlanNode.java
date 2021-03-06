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
package com.espertech.esper.common.internal.epl.join.queryplan;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.util.AgentInstanceContext;
import com.espertech.esper.common.internal.epl.index.base.EventTable;
import com.espertech.esper.common.internal.epl.join.strategy.ExecNode;
import com.espertech.esper.common.internal.epl.virtualdw.VirtualDWView;
import com.espertech.esper.common.internal.view.core.Viewable;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Specification node for a query execution plan to be extended by specific execution specification nodes.
 */
public abstract class QueryPlanNode {
    public final static EPTypeClass EPTYPE = new EPTypeClass(QueryPlanNode.class);

    public abstract ExecNode makeExec(AgentInstanceContext agentInstanceContext, Map<TableLookupIndexReqKey, EventTable>[] indexesPerStream, EventType[] streamTypes, Viewable[] streamViews, VirtualDWView[] viewExternal, Lock[] tableSecondaryIndexLocks);
}
