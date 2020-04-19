/**
 * (c) Copyright 2018, 2019 IBM Corporation
 * 1 New Orchard Road, 
 * Armonk, New York, 10504-1722
 * United States
 * +1 914 499 1900
 * support: Nathaniel Mills wnm3@us.ibm.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.api.jsonata4java.expressions.functions;

import com.api.jsonata4java.expressions.EvaluateRuntimeException;
import com.api.jsonata4java.expressions.ExpressionsVisitor;
import com.api.jsonata4java.expressions.generated.MappingExpressionParser.Function_callContext;
import com.api.jsonata4java.expressions.utils.Constants;
import com.api.jsonata4java.expressions.utils.FunctionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;

/**
 * http://docs.jsonata.org/numeric-functions.html
 * 
 * $floor(number)
 * 
 * Returns the value of number rounded down to the nearest integer that is
 * smaller or equal to number.
 * 
 * If number is not specified (i.e. this function is invoked with no arguments),
 * then the context value is used as the value of number.
 * 
 * Examples
 * 
 * $floor(5)==5 $floor(5.3)==5 $floor(5.8)==5 $floor(-5.3)==-6
 * 
 */
public class FloorFunction extends FunctionBase implements Function {

	public static String ERR_BAD_CONTEXT = String.format(Constants.ERR_MSG_BAD_CONTEXT, Constants.FUNCTION_FLOOR);
	public static String ERR_ARG1BADTYPE = String.format(Constants.ERR_MSG_ARG1_BAD_TYPE, Constants.FUNCTION_FLOOR);
	public static String ERR_ARG2BADTYPE = String.format(Constants.ERR_MSG_ARG2_BAD_TYPE, Constants.FUNCTION_FLOOR);

	public JsonNode invoke(ExpressionsVisitor expressionVisitor, Function_callContext ctx) {
		// Create the variable to return
		JsonNode result = null;

		// Retrieve the number of arguments
		JsonNode argNumber = JsonNodeFactory.instance.nullNode();
		boolean useContext = FunctionUtils.useContextVariable(this, ctx, getSignature());
		int argCount = getArgumentCount(ctx);
		if (useContext) {
			argNumber = FunctionUtils.getContextVariable(expressionVisitor);
			if (argNumber == null) {
				return null;
			}
			if (!argNumber.isNumber()) {
				throw new EvaluateRuntimeException(ERR_BAD_CONTEXT);
			}
			argCount++;
		}

		// Make sure that we have the right number of arguments
		if (argCount == 1) {
			if (!useContext) {
				argNumber = FunctionUtils.getValuesListExpression(expressionVisitor, ctx, 0);
			}
			if (argNumber != null) {
				// Check the type of the argument
				if (argNumber.isNumber()) {
					if (argNumber.isFloatingPointNumber()) {
						// Math.floor only accepts a double
						double floor = Math.floor(argNumber.doubleValue());

						// Create the node to return
						result = new LongNode((long) floor);
					} else {
						// The argument is already an integer... simply return the
						// node
						result = argNumber;
					}
				} else {
					throw new EvaluateRuntimeException(ERR_ARG1BADTYPE);
				}
			}
		} else {
			throw new EvaluateRuntimeException(argCount == 0 ? ERR_BAD_CONTEXT : ERR_ARG2BADTYPE);
		}

		return result;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public String getSignature() {
		// accepts a number (or context variable), returns a number
		return "<n-:n>";
	}
}
