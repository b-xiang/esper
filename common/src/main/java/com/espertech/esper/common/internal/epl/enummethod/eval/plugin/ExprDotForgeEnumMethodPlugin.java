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
package com.espertech.esper.common.internal.epl.enummethod.eval.plugin;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.hook.enummethod.*;
import com.espertech.esper.common.internal.compile.stage2.StatementRawInfo;
import com.espertech.esper.common.internal.compile.stage3.StatementCompileTimeServices;
import com.espertech.esper.common.internal.epl.enummethod.dot.*;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeDesc;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeDescFactory;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeLambdaDesc;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.epl.expression.dot.core.ExprDotNodeUtility;
import com.espertech.esper.common.internal.epl.methodbase.DotMethodFP;
import com.espertech.esper.common.internal.epl.methodbase.DotMethodFPParam;
import com.espertech.esper.common.internal.epl.streamtype.StreamTypeService;
import com.espertech.esper.common.internal.rettype.*;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import com.espertech.esper.common.internal.util.MethodResolver;
import com.espertech.esper.common.internal.util.MethodResolverNoSuchMethodException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExprDotForgeEnumMethodPlugin extends ExprDotForgeEnumMethodBase {

    private final EnumMethodForgeFactory forgeFactory;
    private EnumMethodModeStaticMethod mode;

    public ExprDotForgeEnumMethodPlugin(EnumMethodForgeFactory forgeFactory) {
        this.forgeFactory = forgeFactory;
    }

    @Override
    public void initialize(DotMethodFP footprint, EnumMethodEnum enumMethod, String enumMethodUsedName, EventType inputEventType, Class collectionComponentType, List<ExprNode> parameters, StreamTypeService streamTypeService, StatementRawInfo statementRawInfo, StatementCompileTimeServices services) throws ExprValidationException {
        // validate
        EnumMethodValidateContext ctx = new EnumMethodValidateContext(footprint, inputEventType, collectionComponentType, streamTypeService, enumMethod, parameters, statementRawInfo);
        EnumMethodMode enumMethodMode = forgeFactory.validate(ctx);
        if (!(enumMethodMode instanceof EnumMethodModeStaticMethod)) {
            throw new ExprValidationException("Unexpected EnumMethodMode implementation, expected a provided implementation");
        }
        this.mode = (EnumMethodModeStaticMethod) enumMethodMode;
    }

    public EnumForgeDescFactory getForgeFactory(DotMethodFP footprint, List<ExprNode> parameters, EnumMethodEnum enumMethod, String enumMethodUsedName, EventType inputEventType, Class collectionComponentType, ExprValidationContext validationContext) {
        if (mode == null) {
            throw new IllegalStateException("Initialize did not take place");
        }
        return new EnumForgeDescFactoryPlugin(mode, enumMethodUsedName, footprint, parameters, inputEventType, collectionComponentType, validationContext.getStatementRawInfo(), validationContext.getStatementCompileTimeService());
    }

    public EnumMethodModeStaticMethod getMode() {
        return mode;
    }

    private static class EnumForgeDescFactoryPlugin implements EnumForgeDescFactory {
        private final EnumMethodModeStaticMethod mode;
        private final String enumMethodUsedName;
        private final DotMethodFP footprint;
        private final List<ExprNode> parameters;
        private final EventType inputEventType;
        private final Class collectionComponentType;
        private final StatementRawInfo raw;
        private final StatementCompileTimeServices services;

        public EnumForgeDescFactoryPlugin(EnumMethodModeStaticMethod mode, String enumMethodUsedName, DotMethodFP footprint, List<ExprNode> parameters, EventType inputEventType, Class collectionComponentType, StatementRawInfo raw, StatementCompileTimeServices services) {
            this.mode = mode;
            this.enumMethodUsedName = enumMethodUsedName;
            this.footprint = footprint;
            this.parameters = parameters;
            this.inputEventType = inputEventType;
            this.collectionComponentType = collectionComponentType;
            this.raw = raw;
            this.services = services;
        }

        public EnumForgeLambdaDesc getLambdaStreamTypesForParameter(int parameterNum) {
            DotMethodFPParam desc = footprint.getParameters()[parameterNum];
            if (desc.getLambdaParamNum() == 0) {
                return new EnumForgeLambdaDesc(new EventType[0], new String[0]);
            }
            ExprNode param = parameters.get(parameterNum);
            if (!(param instanceof ExprLambdaGoesNode)) {
                throw new IllegalStateException("Parameter " + parameterNum + " is not a lambda parameter");
            }
            ExprLambdaGoesNode goes = (ExprLambdaGoesNode) param;
            List<String> goesToNames = goes.getGoesToNames();

            // we allocate types for scalar-value-input and index-lambda-parameter-type; for event-input we use the existing input event type
            EventType[] types = new EventType[desc.getLambdaParamNum()];
            String[] names = new String[desc.getLambdaParamNum()];
            for (int i = 0; i < types.length; i++) {
                // obtain lambda parameter type
                EnumMethodLambdaParameterType lambdaParamType = mode.getLambdaParameters().apply(new EnumMethodLambdaParameterDescriptor(parameterNum, i));

                if (lambdaParamType instanceof EnumMethodLambdaParameterTypeValue) {
                    if (inputEventType == null) {
                        types[i] = ExprDotNodeUtility.makeTransientOAType(enumMethodUsedName, goesToNames.get(i), collectionComponentType, raw, services);
                    } else {
                        types[i] = inputEventType;
                    }
                } else if (lambdaParamType instanceof EnumMethodLambdaParameterTypeIndex || lambdaParamType instanceof EnumMethodLambdaParameterTypeSize) {
                    types[i] = ExprDotNodeUtility.makeTransientOAType(enumMethodUsedName, goesToNames.get(i), int.class, raw, services);
                } else if (lambdaParamType instanceof EnumMethodLambdaParameterTypeStateGetter) {
                    EnumMethodLambdaParameterTypeStateGetter getter = (EnumMethodLambdaParameterTypeStateGetter) lambdaParamType;
                    types[i] = ExprDotNodeUtility.makeTransientOAType(enumMethodUsedName, goesToNames.get(i), getter.getType(), raw, services);
                } else {
                    throw new UnsupportedOperationException("Unrecognized lambda parameter type " + lambdaParamType);
                }

                if (types[i] == inputEventType) {
                    names[i] = goesToNames.get(i);
                } else {
                    names[i] = types[i].getName();
                }
            }
            return new EnumForgeLambdaDesc(types, names);
        }

        public EnumForgeDesc makeEnumForgeDesc(List<ExprDotEvalParam> bodiesAndParameters, int streamCountIncoming, StatementCompileTimeServices services)
            throws ExprValidationException {
            // determine static method
            List<Class> parametersNext = new ArrayList<>();

            // first parameter is always the state
            parametersNext.add(mode.getStateClass());

            // second parameter is the value: EventBean for event collection or the collection component type
            if (inputEventType != null) {
                parametersNext.add(EventBean.class);
            } else {
                parametersNext.add(collectionComponentType);
            }

            // remaining parameters are the result of each DotMethodFPParam that returns a lambda (non-lambda is passed to state), always Object typed
            for (ExprDotEvalParam param : bodiesAndParameters) {
                if (param instanceof ExprDotEvalParamLambda) {
                    parametersNext.add(JavaClassHelper.getBoxedType(param.getBody().getForge().getEvaluationType()));
                }
            }

            // obtain service method
            boolean[] noFlags = new boolean[parametersNext.size()];
            Method serviceMethod;
            try {
                serviceMethod = MethodResolver.resolveMethod(mode.getServiceClass(), mode.getMethodName(), parametersNext.toArray(new Class[0]), false, noFlags, noFlags);
            } catch (MethodResolverNoSuchMethodException ex) {
                throw new ExprValidationException("Failed to find service method for enumeration-method '" + mode.getMethodName() + "': " + ex.getMessage(), ex);
            }
            if (serviceMethod.getReturnType() != void.class) {
                throw new ExprValidationException("Failed to validate service method for enumeration-method '" + mode.getMethodName() + "', expected void return type");
            }

            // obtain expected return type
            EPType returnType = mode.getReturnType();
            Class expectedStateReturnType;
            if (returnType instanceof ClassEPType) {
                expectedStateReturnType = ((ClassEPType) returnType).getType();
            } else if (returnType instanceof EventEPType) {
                expectedStateReturnType = EventBean.class;
            } else if (returnType instanceof EventMultiValuedEPType) {
                expectedStateReturnType = Collection.class;
            } else if (returnType instanceof ClassMultiValuedEPType) {
                expectedStateReturnType = ((ClassMultiValuedEPType) returnType).getComponent();
            } else {
                throw new ExprValidationException("Unrecognized return type " + returnType);
            }

            // check state-class
            if (!JavaClassHelper.isSubclassOrImplementsInterface(mode.getStateClass(), EnumMethodState.class)) {
                throw new ExprValidationException("State class " + mode.getStateClass().getName() + " does implement the " + EnumMethodState.class.getName() + " interface");
            }

            EnumForgePlugin forge = new EnumForgePlugin(bodiesAndParameters, mode, expectedStateReturnType, streamCountIncoming, inputEventType);
            return new EnumForgeDesc(returnType, forge);
        }
    }

}
