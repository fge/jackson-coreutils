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

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.EnumMap;
import java.util.Map;

/**
 * Enumeration for the different types of JSON instances which can be
 * encountered.
 *
 * <p>In addition to what the JSON RFC defines, JSON Schema has an {@code
 * integer} type, which is a numeric value without any fraction or exponent
 * part.</p>
 *
 * <p><b>NOTE:</b> will disappear in Jackson 2.2.x, which has an equivalent
 * enumeration</p>
 */

public enum NodeType
{
    /**
     * Array nodes
     */
    ARRAY("array"),
    /**
     * Boolean nodes
     */
    BOOLEAN("boolean"),
    /**
     * Integer nodes
     */
    INTEGER("integer"),
    /**
     * Number nodes (ie, decimal numbers)
     */
    NULL("null"),
    /**
     * Object nodes
     */
    NUMBER("number"),
    /**
     * Null nodes
     */
    OBJECT("object"),
    /**
     * String nodes
     */
    STRING("string");

    /**
     * The name for this type, as encountered in a JSON schema
     */
    private final String name;

    /**
     * Reverse map to find a node type out of this type's name
     */
    private static final Map<String, NodeType> NAME_MAP;

    /**
     * Mapping of {@link JsonToken} back to node types (used in {@link
     * #getNodeType(JsonNode)})
     */
    private static final Map<JsonToken, NodeType> TOKEN_MAP
        = new EnumMap<JsonToken, NodeType>(JsonToken.class);

    static {
        TOKEN_MAP.put(JsonToken.START_ARRAY, ARRAY);
        TOKEN_MAP.put(JsonToken.VALUE_TRUE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_FALSE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_INT, INTEGER);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_FLOAT, NUMBER);
        TOKEN_MAP.put(JsonToken.VALUE_NULL, NULL);
        TOKEN_MAP.put(JsonToken.START_OBJECT, OBJECT);
        TOKEN_MAP.put(JsonToken.VALUE_STRING, STRING);

        final ImmutableMap.Builder<String, NodeType> builder
            = ImmutableMap.builder();

        for (final NodeType type: NodeType.values())
            builder.put(type.name, type);

        NAME_MAP = builder.build();
    }

    NodeType(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Given a type name, return the corresponding node type
     *
     * @param name the type name
     * @return the node type, or null if not found
     */
    public static NodeType fromName(final String name)
    {
        return NAME_MAP.get(name);
    }

    /**
     * Given a {@link JsonNode} as an argument, return its type. The argument
     * MUST NOT BE NULL, and MUST NOT be a {@link MissingNode}
     *
     * @param node the node to determine the type of
     * @return the type for this node
     */
    public static NodeType getNodeType(final JsonNode node)
    {
        final JsonToken token = node.asToken();
        final NodeType ret = TOKEN_MAP.get(token);

        Preconditions.checkNotNull(ret, "unhandled token type " + token);

        return ret;
    }
}
