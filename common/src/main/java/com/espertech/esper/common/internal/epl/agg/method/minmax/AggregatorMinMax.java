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
package com.espertech.esper.common.internal.epl.agg.method.minmax;

import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMemberCol;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenCtor;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionMember;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.collection.SortedRefCountedSet;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregatorMethodWDistinctWFilterWValueBase;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.epl.expression.core.MinMaxTypeEnum;
import com.espertech.esper.common.internal.serde.compiletime.sharable.CodegenSharableSerdeClassTyped;
import com.espertech.esper.common.internal.serde.compiletime.resolve.DataInputOutputSerdeForge;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.epl.agg.method.core.AggregatorCodegenUtil.rowDotMember;
import static com.espertech.esper.common.internal.serde.compiletime.sharable.CodegenSharableSerdeClassTyped.CodegenSharableSerdeName.SORTEDREFCOUNTEDSET;

/**
 * Min/max aggregator for all values.
 */
public class AggregatorMinMax extends AggregatorMethodWDistinctWFilterWValueBase {
    private final AggregationForgeFactoryMinMax factory;
    private CodegenExpressionMember refSet;
    private CodegenExpressionField serdeField;

    public AggregatorMinMax(AggregationForgeFactoryMinMax factory, EPTypeClass optionalDistinctValueType, DataInputOutputSerdeForge optionalDistinctSerde, boolean hasFilter, ExprNode optionalFilter) {
        super(optionalDistinctValueType, optionalDistinctSerde, hasFilter, optionalFilter);
        this.factory = factory;
    }

    public void initForgeFiltered(int col, CodegenCtor rowCtor, CodegenMemberCol membersColumnized, CodegenClassScope classScope) {
        this.refSet = membersColumnized.addMember(col, SortedRefCountedSet.EPTYPE, "refSet");
        this.serdeField = classScope.addOrGetFieldSharable(new CodegenSharableSerdeClassTyped(SORTEDREFCOUNTEDSET, factory.type, factory.serde, classScope));
        rowCtor.getBlock().assignRef(refSet, newInstance(SortedRefCountedSet.EPTYPE));
    }

    protected void applyEvalEnterNonNull(CodegenExpressionRef value, EPType valueType, CodegenMethod method, ExprForgeCodegenSymbol symbols, ExprForge[] forges, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(refSet, "add", value);
    }

    protected void applyTableEnterNonNull(CodegenExpressionRef value, EPType[] evaluationTypes, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(refSet, "add", value);
    }

    protected void applyEvalLeaveNonNull(CodegenExpressionRef value, EPType valueType, CodegenMethod method, ExprForgeCodegenSymbol symbols, ExprForge[] forges, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(refSet, "remove", value);
    }

    protected void applyTableLeaveNonNull(CodegenExpressionRef value, EPType[] evaluationTypes, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(refSet, "remove", value);
    }

    protected void clearWODistinct(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(refSet, "clear");
    }

    public void getValueCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().methodReturn(exprDotMethod(refSet, factory.getParent().getMinMaxTypeEnum() == MinMaxTypeEnum.MAX ? "maxValue" : "minValue"));
    }

    protected void writeWODistinct(CodegenExpressionRef row, int col, CodegenExpressionRef output, CodegenExpressionRef unitKey, CodegenExpressionRef writer, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().exprDotMethod(serdeField, "write", rowDotMember(row, refSet), output, unitKey, writer);
    }

    protected void readWODistinct(CodegenExpressionRef row, int col, CodegenExpressionRef input, CodegenExpressionRef unitKey, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().assignRef(rowDotMember(row, refSet), cast(SortedRefCountedSet.EPTYPE, exprDotMethod(serdeField, "read", input, unitKey)));
    }
}
