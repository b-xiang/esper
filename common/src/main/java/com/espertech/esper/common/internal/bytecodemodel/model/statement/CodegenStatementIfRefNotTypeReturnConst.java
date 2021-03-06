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

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.espertech.esper.common.internal.bytecodemodel.core.CodeGenerationHelper.appendClassName;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionUtil.renderConstant;

public class CodegenStatementIfRefNotTypeReturnConst extends CodegenStatementBase {

    private final String var;
    private final EPTypeClass type;
    private final Object constant;

    public CodegenStatementIfRefNotTypeReturnConst(String var, EPTypeClass type, Object constant) {
        this.var = var;
        this.type = type;
        this.constant = constant;
    }

    public void renderStatement(StringBuilder builder, Map<Class, String> imports, boolean isInnerClass) {
        builder.append("if (!(").append(var).append(" instanceof ");
        appendClassName(builder, type, imports).append(")) return ");
        renderConstant(builder, constant, imports, isInnerClass);
    }

    public void mergeClasses(Set<Class> classes) {
        type.traverseClasses(classes::add);
    }

    public void traverseExpressions(Consumer<CodegenExpression> consumer) {
    }
}
