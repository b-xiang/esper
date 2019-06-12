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
package com.espertech.esper.common.internal.epl.rowrecog.expr;

import java.io.StringWriter;

/**
 * Concatenation of atoms in a regular expression tree.
 */
public class RowRecogExprNodeConcatenation extends RowRecogExprNode {
    /**
     * Ctor.
     */
    public RowRecogExprNodeConcatenation() {
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
        String delimiter = "";
        for (RowRecogExprNode node : this.getChildNodes()) {
            writer.append(delimiter);
            node.toEPL(writer, getPrecedence());
            delimiter = " ";
        }
    }

    public RowRecogExprNodePrecedenceEnum getPrecedence() {
        return RowRecogExprNodePrecedenceEnum.CONCATENATION;
    }
}
