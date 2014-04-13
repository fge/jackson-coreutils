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

/**
 * Jackson utility classes
 *
 * <p>{@link com.github.fge.jackson.JsonLoader} contains various methods to load
 * JSON documents as {@link com.fasterxml.jackson.databind.JsonNode}s. It uses
 * a {@link com.github.fge.jackson.JsonNodeReader} (as such, parsing {@code []]}
 * will generate an error where Jackson normally does not).</p>
 *
 * <p>You will also want to use {@link com.github.fge.jackson.JacksonUtils}
 * to grab a node factory, reader and pretty printer for anything JSON. Compared
 * to the basic Jackson's {@link com.fasterxml.jackson.databind.ObjectMapper},
 * the one provided by {@link com.github.fge.jackson.JacksonUtils} deserializes
 * all floating point numbers as {@link java.math.BigDecimal}s by default. This
 * is done using {@link
 * com.fasterxml.jackson.databind.DeserializationFeature#USE_BIG_DECIMAL_FOR_FLOATS}.
 * </p>
 *
 * <p>{@link com.github.fge.jackson.JsonNumEquals} is an {@link
 * com.google.common.base.Equivalence} over {@link
 * com.fasterxml.jackson.databind.JsonNode} for recursive equivalence of JSON
 * number values.</p>
 *
 * <p>Finally, {@link com.github.fge.jackson.NodeType} is a utility enumeration
 * which distinguishes between all JSON node types defined by RFC 7159, plus
 * {@code integer} (used by JSON Schema). Note that since Jackson 2.2, there is
 * also {@link com.fasterxml.jackson.databind.JsonNode#getNodeType()}, but it
 * does not make a difference between {@code number} and {@code integer}.</p>
 */
package com.github.fge.jackson;
