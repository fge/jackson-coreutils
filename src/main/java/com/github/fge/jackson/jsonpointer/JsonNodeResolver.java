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

package com.github.fge.jackson.jsonpointer;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.concurrent.Immutable;

/**
 * Implementation of {@link TokenResolver} for {@link JsonNode}
 *
 * <p>The JSON Pointer specification specifies that for arrays, indices must
 * not have leading zeroes (save for {@code 0} itself). This class handles
 * this.</p>
 */
@Immutable
public final class JsonNodeResolver
    extends TokenResolver<JsonNode>
{
    /**
     * Zero
     */
    private static final char ZERO = '0';

    public JsonNodeResolver(final ReferenceToken token)
    {
        super(token);
    }

    @Override
    public JsonNode get(final JsonNode node)
    {
        if (node == null || !node.isContainerNode())
            return null;
        final String raw = token.getRaw();
        return node.isObject() ? node.get(raw) : node.get(arrayIndexFor(raw));
    }

    /**
     * Return an array index corresponding to the given (raw) reference token
     *
     * <p>If no array index can be found, -1 is returned. As the result is used
     * with {@link JsonNode#get(int)}, we are guaranteed correct results, since
     * this will return {@code null} in this case.</p>
     *
     * @param raw the raw token, as a string
     * @return the index, or -1 if the index is invalid
     */
    private static int arrayIndexFor(final String raw)
    {
        /*
         * Empty? No dice.
         */
        if (raw.isEmpty())
            return -1;
        /*
         * Leading zeroes are not allowed in number-only refTokens for arrays.
         * But then, 0 followed by anything else than a number is invalid as
         * well. So, if the string starts with '0', return 0 if the token length
         * is 1 or -1 otherwise.
         */
        if (raw.charAt(0) == ZERO)
            return raw.length() == 1 ? 0 : -1;

        /*
         * Otherwise, parse as an int. If we can't, -1.
         */
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
