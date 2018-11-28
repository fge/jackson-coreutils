/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.util.*;

public final class SampleNodeProvider
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();
    private static final Map<NodeType, JsonNode> SAMPLE_DATA;

    private SampleNodeProvider()
    {
    }

    static {
        SAMPLE_DATA = new EnumMap<NodeType, JsonNode>(NodeType.class);

        SAMPLE_DATA.put(NodeType.ARRAY, FACTORY.arrayNode());
        SAMPLE_DATA.put(NodeType.BOOLEAN, FACTORY.booleanNode(true));
        SAMPLE_DATA.put(NodeType.INTEGER, FACTORY.numberNode(0));
        SAMPLE_DATA.put(NodeType.NULL, FACTORY.nullNode());
        SAMPLE_DATA.put(NodeType.NUMBER,
            FACTORY.numberNode(new BigDecimal("1.1")));
        SAMPLE_DATA.put(NodeType.OBJECT, FACTORY.objectNode());
        SAMPLE_DATA.put(NodeType.STRING, FACTORY.textNode(""));
    }


    // FIXME: IDEA warns about "overloaded vararg method" even though the types
    // differ...
    public static Iterator<Object[]> getSamples(final EnumSet<NodeType> types)
    {
        final Map<NodeType, JsonNode> map = new EnumMap<NodeType, JsonNode>(SAMPLE_DATA);
        map.keySet().retainAll(types);

        final ArrayList<Object[]> samples = new ArrayList<Object[]>();
        for (final JsonNode input : map.values()) {
            samples.add(new Object[] { input });
        }

        return samples.iterator();
    }

    public static Iterator<Object[]> getSamplesExcept(
        final EnumSet<NodeType> types)
    {
        return getSamples(EnumSet.complementOf(types));
    }

    public static Iterator<Object[]> getSamples(final NodeType first,
        final NodeType... other)
    {
        return getSamples(EnumSet.of(first, other));
    }

    public static Iterator<Object[]> getSamplesExcept(final NodeType first,
        final NodeType... other)
    {
        return getSamples(EnumSet.complementOf(EnumSet.of(first, other)));
    }
}
