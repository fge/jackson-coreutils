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

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link TreePointer} for {@link JsonNode}
 *
 * <p>This is the "original" JSON Pointer in that it addresses JSON documents.
 * </p>
 *
 * <p>It also has a lot of utility methods covering several usage scenarios.</p>
 */
@Immutable
public final class JsonPointer
    extends TreePointer<JsonNode>
{
    /**
     * The empty JSON Pointer
     */
    private static final JsonPointer EMPTY
        = new JsonPointer(Collections.<TokenResolver<JsonNode>>emptyList());

    /**
     * Return an empty JSON Pointer
     *
     * @return an empty, statically allocated JSON Pointer
     */
    public static JsonPointer empty()
    {
        return EMPTY;
    }

    /**
     * Build a JSON Pointer out of a series of reference tokens
     *
     * <p>These tokens can be everything; be sure however that they implement
     * {@link Object#toString()} correctly!</p>
     *
     * <p>Each of these tokens are treated as <b>raw</b> tokens (ie, not
     * encoded).</p>
     *
     * @param first the first token
     * @param other other tokens
     * @return a JSON Pointer
     * @throws NullPointerException one input token is null
     */
    public static JsonPointer of(final Object first, final Object... other)
    {
        final List<ReferenceToken> tokens = new ArrayList<ReferenceToken>();

        tokens.add(ReferenceToken.fromRaw(first.toString()));

        for (final Object o: other)
            tokens.add(ReferenceToken.fromRaw(o.toString()));

        return new JsonPointer(fromTokens(tokens));
    }

    /**
     * The main constructor
     *
     * @param input the input string
     * @throws JsonPointerException malformed JSON Pointer
     * @throws NullPointerException null input
     */
    public JsonPointer(final String input)
        throws JsonPointerException
    {
        this(fromTokens(tokensFromInput(input)));
    }

    /**
     * Alternate constructor
     *
     * <p>This calls {@link TreePointer#TreePointer(TreeNode, List)} with a
     * {@link MissingNode} as the missing tree node.</p>
     *
     * @param tokenResolvers the list of token resolvers
     */
    public JsonPointer(final List<TokenResolver<JsonNode>> tokenResolvers)
    {
        super(MissingNode.getInstance(), tokenResolvers);
    }

    /**
     * Return a new pointer with a new token appended
     *
     * @param raw the raw token to append
     * @return a new pointer
     * @throws NullPointerException input is null
     */
    public JsonPointer append(final String raw)
    {
        final ReferenceToken refToken = ReferenceToken.fromRaw(raw);
        final JsonNodeResolver resolver = new JsonNodeResolver(refToken);
        final List<TokenResolver<JsonNode>> list
            = new ArrayList<TokenResolver<JsonNode>>();
        for (final TokenResolver<JsonNode> tokenResolver : tokenResolvers) {
            if (tokenResolver != null) {
                list.add(tokenResolver);
            } else {
                throw new NullPointerException();
            }
        }
        list.add(resolver);
        return new JsonPointer(list);
    }

    /**
     * Return a new pointer with a new integer token appended
     *
     * @param index the integer token to append
     * @return a new pointer
     */
    public JsonPointer append(final int index)
    {
        return append(Integer.toString(index));
    }

    /**
     * Return a new pointer with another pointer appended
     *
     * @param other the other pointer
     * @return a new pointer
     * @throws NullPointerException other pointer is null
     */
    public JsonPointer append(final JsonPointer other)
    {
        BUNDLE.checkNotNull(other, "nullInput");
        final List<TokenResolver<JsonNode>> list
            = new ArrayList<TokenResolver<JsonNode>>();
        for (final TokenResolver<JsonNode> tokenResolver : tokenResolvers) {
            if (tokenResolver != null) {
                list.add(tokenResolver);
            } else {
                throw new NullPointerException();
            }
        }
        list.addAll(other.tokenResolvers);
        return new JsonPointer(list);
    }

    /**
     * Return the immediate parent of this JSON Pointer
     *
     * <p>The parent of the empty pointer is itself.</p>
     *
     * @return a new JSON Pointer representing the parent of the current one
     */
    public JsonPointer parent()
    {
        final int size = tokenResolvers.size();
        return size <= 1 ? EMPTY
            : new JsonPointer(tokenResolvers.subList(0, size - 1));
    }

    /**
     * Build a list of token resolvers from a list of reference tokens
     *
     * <p>Here, the token resolvers are {@link JsonNodeResolver}s.</p>
     *
     * @param tokens the token list
     * @return a (mutable) list of token resolvers
     */
    private static List<TokenResolver<JsonNode>> fromTokens(
        final List<ReferenceToken> tokens)
    {
        final List<TokenResolver<JsonNode>> list = new ArrayList<TokenResolver<JsonNode>>();
        for (final ReferenceToken token: tokens)
            list.add(new JsonNodeResolver(token));
        return list;
    }
}
