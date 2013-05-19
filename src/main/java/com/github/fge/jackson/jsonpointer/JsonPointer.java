/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jackson.jsonpointer;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.jcip.annotations.Immutable;

import java.util.List;

import static com.github.fge.jackson.jsonpointer.JsonPointerMessages.*;

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
        = new JsonPointer(ImmutableList.<TokenResolver<JsonNode>>of());

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
        final List<ReferenceToken> tokens = Lists.newArrayList();

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
            = Lists.newArrayList(tokenResolvers);
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
        Preconditions.checkNotNull(other, NULL_INPUT);
        final List<TokenResolver<JsonNode>> list
            = Lists.newArrayList(tokenResolvers);
        list.addAll(other.tokenResolvers);
        return new JsonPointer(list);
    }

    /**
     * Return a new pointer with last token removed; if there
     * is one or no tokens to remove, the {@link JsonPointer#empty()}
     * pointer is returned
     * 
     * @return a new pointer or {@link JsonPointer#empty()}
     */
    public JsonPointer parent()
    {
        final int size = tokenResolvers.size();
        return size <= 1 ? EMPTY : new JsonPointer(tokenResolvers.subList(0, size - 1));
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
        final List<TokenResolver<JsonNode>> list = Lists.newArrayList();
        for (final ReferenceToken token: tokens)
            list.add(new JsonNodeResolver(token));
        return list;
    }
}
