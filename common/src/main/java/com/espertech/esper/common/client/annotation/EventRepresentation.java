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
package com.espertech.esper.common.client.annotation;

import com.espertech.esper.common.client.util.EventUnderlyingType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that can be attached to specify which underlying event representation to use for events.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventRepresentation {
    /**
     * Define the event underlying type
     *
     * @return event underlying type
     */
    EventUnderlyingType value();
}
