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
package com.espertech.esper.common.internal.epl.expression.ops;

import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityPrint;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.support.SupportExprValidationContextFactory;
import com.espertech.esper.common.internal.supportunit.util.SupportBoolExprNode;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNode;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNodeUtil;
import junit.framework.TestCase;

public class TestExprAndNode extends TestCase {
    private ExprAndNode andNode;

    public void setUp() {
        andNode = new ExprAndNodeImpl();
    }

    public void testGetType() {
        assertEquals(EPTypePremade.BOOLEANBOXED.getEPType(), andNode.getForge().getEvaluationType());
    }

    public void testValidate() throws Exception {
        // test success
        andNode.addChildNode(new SupportExprNode(Boolean.class));
        andNode.addChildNode(new SupportExprNode(Boolean.class));
        andNode.validate(SupportExprValidationContextFactory.makeEmpty());

        // test failure, type mismatch
        andNode.addChildNode(new SupportExprNode(String.class));
        try {
            andNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }

        // test failed - with just one child
        andNode = new ExprAndNodeImpl();
        andNode.addChildNode(new SupportExprNode(Boolean.class));
        try {
            andNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }
    }

    public void testEvaluate() throws Exception {
        andNode.addChildNode(new SupportBoolExprNode(true));
        andNode.addChildNode(new SupportBoolExprNode(true));
        SupportExprNodeUtil.validate(andNode);
        assertTrue((Boolean) andNode.getForge().getExprEvaluator().evaluate(null, false, null));

        andNode = new ExprAndNodeImpl();
        andNode.addChildNode(new SupportBoolExprNode(true));
        andNode.addChildNode(new SupportBoolExprNode(false));
        SupportExprNodeUtil.validate(andNode);
        assertFalse((Boolean) andNode.getForge().getExprEvaluator().evaluate(null, false, null));
    }

    public void testToExpressionString() throws Exception {
        andNode.addChildNode(new SupportExprNode(true));
        andNode.addChildNode(new SupportExprNode(false));

        assertEquals("true and false", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(andNode));
    }

    public void testEqualsNode() {
        assertTrue(andNode.equalsNode(new ExprAndNodeImpl(), false));
        assertFalse(andNode.equalsNode(new ExprOrNode(), false));
    }
}
