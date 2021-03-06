/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.test.core.impl.config.converter;

import org.apache.deltaspike.core.impl.config.converter.PatternConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.9.5
 */
public class PatternConverterTest
{
    private PatternConverter converter;

    @Before
    public void before()
    {
        converter = new PatternConverter();
    }

    @Test
    public void testConverteringPattern()
    {
        final String expected = "(?i)Ow.+O";
        final String actual = converter.convert("(?i)Ow.+O").toString();

        Assert.assertEquals(expected, actual);
    }
}
