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

import org.antlr.v4.runtime.tree.ParseTree;

import com.api.jsonata4java.expressions.EvaluateRuntimeException;
import com.api.jsonata4java.expressions.ExpressionsVisitor;
import com.api.jsonata4java.expressions.generated.MappingExpressionParser.Function_callContext;
import com.api.jsonata4java.expressions.utils.Constants;
import com.api.jsonata4java.expressions.utils.FunctionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * From http://docs.jsonata.org/string-functions.html:
 * 
 * $lowercase(str)
 * 
 * Returns a string with all the characters of str converted to lowercase. If
 * str is not specified (i.e. this function is invoked with no arguments), then
 * the context value is used as the value of str. An error is thrown if str is
 * not a string.
 * 
 * Examples
 * 
 * $lowercase("Hello World")=="hello world"
 * 
 */
public class LowercaseFunction extends FunctionBase implements Function {

	public static String ERR_BAD_CONTEXT = String.format(Constants.ERR_MSG_BAD_CONTEXT, Constants.FUNCTION_LOWERCASE);
	public static String ERR_ARG1BADTYPE = String.format(Constants.ERR_MSG_ARG1_BAD_TYPE, Constants.FUNCTION_LOWERCASE);
	public static String ERR_ARG2BADTYPE = String.format(Constants.ERR_MSG_ARG2_BAD_TYPE, Constants.FUNCTION_LOWERCASE);

	public JsonNode invoke(ExpressionsVisitor expressionVisitor, Function_callContext ctx) {
		// Create the variable to return
		JsonNode result = null;

		// Retrieve the number of arguments
		JsonNode argString = JsonNodeFactory.instance.nullNode();
		boolean useContext = FunctionUtils.useContextVariable(this, ctx, getSignature());
		int argCount = getArgumentCount(ctx);
		if (useContext) {
			argString = FunctionUtils.getContextVariable(expressionVisitor);
			argCount++;
		}

		// Make sure that we have the right number of arguments
		if (argCount == 1) {
			if (!useContext) {
				argString = FunctionUtils.getValuesListExpression(expressionVisitor, ctx, 0);
			}
			if (argString != null) {
				if (argString.isTextual()) {
					final String str = argString.textValue();
					result = new TextNode(str.toLowerCase());
				} else {
					throw new EvaluateRuntimeException(ERR_ARG1BADTYPE);
				}
			}
		} else if (argCount == 2) {
         if (!useContext) {
            argString = FunctionUtils.getValuesListExpression(expressionVisitor, ctx, 0);
         }
         ParseTree value = ctx.exprValues().exprList();
         expressionVisitor.getContextStack().push(argString);
         result = expressionVisitor.visit(value);
         if (result != null && result.isTextual()) {
            result = new TextNode(result.textValue().toLowerCase());
         }
         expressionVisitor.getContextStack().pop();
	   }else {
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
		// accepts a string (or context variable), returns a string
		return "<s-:s>";
	}
}
