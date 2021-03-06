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
package com.espertech.esper.common.internal.epl.resultset.select.eval;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.context.module.EPStatementInitServices;
import com.espertech.esper.common.internal.epl.resultset.select.core.SelectExprForgeContext;
import com.espertech.esper.common.internal.event.core.EventTypeUtility;
import com.espertech.esper.common.internal.event.core.WrapperEventType;
import com.espertech.esper.common.internal.event.json.core.JsonEventType;

import java.util.Collections;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class SelectEvalInsertNoWildcardSingleColCoercionJsonWrap extends SelectEvalBaseFirstPropFromWrap {

    public SelectEvalInsertNoWildcardSingleColCoercionJsonWrap(SelectExprForgeContext selectExprForgeContext, WrapperEventType wrapper) {
        super(selectExprForgeContext, wrapper);
    }

    protected CodegenExpression processFirstColCodegen(EPTypeClass evaluationType, CodegenExpression expression, CodegenExpression resultEventType, CodegenExpression eventBeanFactory, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        CodegenExpressionField memberUndType = codegenClassScope.addFieldUnshared(true, JsonEventType.EPTYPE, cast(JsonEventType.EPTYPE, EventTypeUtility.resolveTypeCodegen(wrapper.getUnderlyingEventType(), EPStatementInitServices.REF)));
        CodegenExpressionField memberWrapperType = codegenClassScope.addFieldUnshared(true, WrapperEventType.EPTYPE, cast(WrapperEventType.EPTYPE, EventTypeUtility.resolveTypeCodegen(wrapper, EPStatementInitServices.REF)));
        CodegenMethod method = codegenMethodScope.makeChild(EventBean.EPTYPE, this.getClass(), codegenClassScope).addParam(evaluationType, "result").getBlock()
                .declareVar(EPTypePremade.STRING.getEPType(), "json", cast(EPTypePremade.STRING.getEPType(), ref("result")))
                .ifNullReturnNull(ref("json"))
                .declareVar(EPTypePremade.OBJECT.getEPType(), "und", exprDotMethod(memberUndType, "parse", ref("json")))
                .declareVar(EventBean.EPTYPE, "bean", exprDotMethod(eventBeanFactory, "adapterForTypedJson", ref("und"), memberUndType))
                .methodReturn(exprDotMethod(eventBeanFactory, "adapterForTypedWrapper", ref("bean"), staticMethod(Collections.class, "emptyMap"), memberWrapperType));
        return localMethodBuild(method).pass(expression).call();
    }
}
