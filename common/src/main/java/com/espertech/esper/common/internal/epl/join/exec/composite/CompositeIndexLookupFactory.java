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
package com.espertech.esper.common.internal.epl.join.exec.composite;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.collection.MultiKeyFromObjectArray;
import com.espertech.esper.common.internal.epl.join.exec.util.RangeIndexLookupValue;

import java.util.ArrayList;
import java.util.List;

public class CompositeIndexLookupFactory {

    public static CompositeIndexLookup make(Object[] keyValues, MultiKeyFromObjectArray multiKeyTransform, RangeIndexLookupValue[] rangeValues, EPTypeClass[] rangeCoercion) {
        // construct chain
        List<CompositeIndexLookup> queries = new ArrayList<CompositeIndexLookup>();
        if (keyValues != null && keyValues.length > 0) {
            queries.add(new CompositeIndexLookupKeyed(keyValues, multiKeyTransform));
        }
        for (int i = 0; i < rangeValues.length; i++) {
            queries.add(new CompositeIndexLookupRange(rangeValues[i], rangeCoercion[i]));
        }

        // Hook up as chain for remove
        CompositeIndexLookup last = null;
        for (CompositeIndexLookup action : queries) {
            if (last != null) {
                last.setNext(action);
            }
            last = action;
        }
        return queries.get(0);
    }
}
