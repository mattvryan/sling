/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.api.wrappers.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Array;

import org.apache.commons.lang3.ClassUtils;

/**
 * Tests all permutations of object conversions between single values and array types, and null handling.
 */
class Convert {
    
    @SuppressWarnings("unchecked")
    public static class ConversionAssert<T,U> {
        private final T input1;
        private final T input2;
        private Class<T> inputType;
        private U expected1;
        private U expected2;
        private U nullValue;
        private Class<U> expectedType;
        
        private ConversionAssert(T input1, T input2, boolean inputTypePrimitive) {
            this.input1 = input1;
            this.input2 = input2;
            this.inputType = (Class<T>)input1.getClass();
            if (inputTypePrimitive) {
                this.inputType = (Class<T>)ClassUtils.wrapperToPrimitive(this.inputType);
            }
        }
        
        private void expected(U expected1, U expected2, boolean expectedTypePrimitive) {
            this.expected1 = expected1;
            this.expected2 = expected2;
            this.expectedType = (Class<U>)expected1.getClass();
            if (expectedTypePrimitive) {
                expectedType = (Class<U>)ClassUtils.wrapperToPrimitive(this.expectedType);
            }
        }
        
        /**
         * @param expected1 Singleton or first array expected result value
         * @param expected2 Second array expected result value
         * @return this
         */
        public ConversionAssert<T,U> to(U expected1, U expected2) {
            expected(expected1, expected2, false);
            return this;
        }

        /**
         * @param expected1 Singleton or first array expected result value
         * @param expected2 Second array expected result value
         * @return this
         */
        public ConversionAssert<T,U> toPrimitive(U expected1, U expected2) {
            expected(expected1, expected2, true);
            return this;
        }
        
        /**
         * @param expected1 Singleton or first array expected result value
         * @param expected2 Second array expected result value
         * @return this
         */
        public ConversionAssert<T,U> toNull(Class<U> expectedType) {
            expected1 = null;
            expected2 = null;
            this.expectedType = expectedType;
            return this;
        }
        
        /**
         * @param nullValue Result value in case of null
         */
        public ConversionAssert<T,U> nullValue(U nullValue) {
            this.nullValue = nullValue;
            return this;
        }
        
        /**
         * Do assertion
         */
        public void test() {
            Class<U[]> expectedArrayType = (Class<U[]>)Array.newInstance(this.expectedType, 0).getClass();
            assertPermuations(input1, input2, inputType, expected1, expected2, nullValue, expectedType, expectedArrayType);
        }
    }

    /**
     * @param input1 Singleton or first array input value
     * @param input2 Second array input value
     */
    public static <T,U> ConversionAssert<T,U> from(T input1, T input2) {
        return new ConversionAssert<T,U>(input1, input2, false);
    }

    /**
     * @param input1 Singleton or first array input value
     * @param input2 Second array input value
     */
    public static <T,U> ConversionAssert<T,U> fromPrimitive(T input1, T input2) {
        return new ConversionAssert<T,U>(input1, input2, true);
    }

    private static <T,U> void assertPermuations(T input1, T input2, Class<T> inputType,
            U expected1, U expected2, U nullValue, Class<U> expectedType, Class<U[]> expectedArrayType) {
        
        // single value to single value
        assertConversion(expected1, input1, expectedType);
        
        // single value to array
        Object expectedSingletonArray = Array.newInstance(expectedType, 1);
        Array.set(expectedSingletonArray, 0, expected1);
        assertConversion(expectedSingletonArray, input1, expectedArrayType);
        
        // array to array
        Object inputDoubleArray = Array.newInstance(inputType, 2);
        Array.set(inputDoubleArray, 0, input1);
        Array.set(inputDoubleArray, 1, input2);
        Object expectedDoubleArray = Array.newInstance(expectedType, 2);
        Array.set(expectedDoubleArray, 0,  expected1);
        Array.set(expectedDoubleArray, 1,  expected2);
        assertConversion(expectedDoubleArray, inputDoubleArray, expectedArrayType);
        
        // array to single (first) value
        assertConversion(expected1, inputDoubleArray, expectedType);
        
        // null to single value
        assertConversion(nullValue, null, expectedType);
        
        // null to array
        assertConversion(null, null, expectedArrayType);
        
        // empty array to single value
        Object inputEmptyArray = Array.newInstance(inputType, 0);
        assertConversion(nullValue, inputEmptyArray, expectedType);

        // empty array to array
        assertConversion(null, inputEmptyArray, expectedArrayType);
    }
    
    @SuppressWarnings("unchecked")
    private static <T,U> void assertConversion(Object expected, Object input, Class<U> type) {
        U result = ObjectConverter.convert(input, type);
        String msg = "Convert '" + toString(input) + "' to " + type.getSimpleName();
        if (expected == null) {
            assertNull(msg, result);
        }
        else if (expected.getClass().isArray()) {
            assertArrayEquals(msg, (U[])expected, (U[])result);
        }
        else {
            assertEquals(msg, expected, result);
        }
    }
    
    private static String toString(Object input) {
        if (input == null) {
            return "null";
        }
        else if (input.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i=0; i<Array.getLength(input); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(Array.get(input, i));
            }
            sb.append("]");
            return sb.toString();
        }
        else {
            return input.toString();
        }
    }
    
}