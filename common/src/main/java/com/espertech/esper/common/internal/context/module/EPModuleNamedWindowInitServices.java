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
package com.espertech.esper.common.internal.context.module;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.epl.namedwindow.path.NamedWindowCollector;
import com.espertech.esper.common.internal.event.path.EventTypeResolver;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.ref;

public interface EPModuleNamedWindowInitServices {
    EPTypeClass EPTYPE = new EPTypeClass(EPModuleNamedWindowInitServices.class);
    CodegenExpressionRef REF = ref("epModuleNamedWindowInitServices");

    String GETNAMEDWINDOWCOLLECTOR = "getNamedWindowCollector";
    String GETEVENTTYPERESOLVER = EPStatementInitServices.GETEVENTTYPERESOLVER;

    NamedWindowCollector getNamedWindowCollector();

    EventTypeResolver getEventTypeResolver();
}
