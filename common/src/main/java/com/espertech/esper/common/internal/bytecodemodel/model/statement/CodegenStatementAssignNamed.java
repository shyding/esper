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
package com.espertech.esper.common.internal.bytecodemodel.model.statement;

import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CodegenStatementAssignNamed extends CodegenStatementBase {
    private final CodegenExpression lhs;
    private final CodegenExpression assignment;

    public CodegenStatementAssignNamed(CodegenExpression lhs, CodegenExpression assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not provided");
        }
        this.lhs = lhs;
        this.assignment = assignment;
    }

    public void renderStatement(StringBuilder builder, Map<Class, String> imports, boolean isInnerClass) {
        lhs.render(builder, imports, isInnerClass);
        builder.append("=");
        assignment.render(builder, imports, isInnerClass);
    }

    public void mergeClasses(Set<Class> classes) {
        assignment.mergeClasses(classes);
    }

    public void traverseExpressions(Consumer<CodegenExpression> consumer) {
        consumer.accept(lhs);
        consumer.accept(assignment);
    }
}
