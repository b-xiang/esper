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
package com.espertech.esper.regressionlib.support.extend.vdw;

import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowFactory;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowFactoryFactory;
import com.espertech.esper.common.client.hook.vdw.VirtualDataWindowFactoryFactoryContext;
import com.espertech.esper.common.client.type.EPTypeClass;

public class SupportVirtualDWInvalidFactoryFactory implements VirtualDataWindowFactoryFactory {
    public final static EPTypeClass EPTYPE = new EPTypeClass(SupportVirtualDWInvalidFactoryFactory.class
    );
    public VirtualDataWindowFactory createFactory(VirtualDataWindowFactoryFactoryContext ctx) {
        return new SupportVirtualDWInvalidFactory();
    }
}
