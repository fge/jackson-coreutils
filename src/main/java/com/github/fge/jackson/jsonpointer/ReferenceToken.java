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

import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;
import java.nio.CharBuffer;
import java.util.List;


/**
 * One JSON Pointer reference token
 *
 * <p>This class represents one reference token. It has no publicly available
 * constructor; instead, it has static factory methods used to generate tokens
 * depending on whether the input is a decoded (raw) token or an encoded
 * (cooked) one, or even an integer.</p>
 *
 * <p>The only characters to encode in a raw token are {@code /} (which becomes
 * {@code ~1}) and {@code ~} (which becomes {@code ~0}).</p>
 *
 * <p>Note that a reference token <b>may</b> be empty (empty object member names
 * are legal!).</p>
 */
@Immutable
public final class ReferenceToken
{
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPointerMessages.class);
    /**
     * The escape character in a cooked token
     */
    private static final char ESCAPE = '~';

    /**
     * List of encoding characters in a cooked token
     */
    private static final List<Character> ENCODED = ImmutableList.of('0', '1');

    /**
     * List of sequences to encode in a raw token
     *
     * <p>This list and {@link #ENCODED} have matching indices on purpose.</p>
     */
    private static final List<Character> DECODED = ImmutableList.of('~', '/');

    /**
     * The cooked representation of that token
     *
     * @see #toString()
     */
    private final String cooked;

    /**
     * The raw representation of that token
     *
     * @see #hashCode()
     * @see #equals(Object)
     */
    private final String raw;

    /**
     * The only constructor, private by design
     *
     * @param cooked the cooked representation of that token
     * @param raw the raw representation of that token
     */
    private ReferenceToken(final String cooked, final String raw)
    {
        this.cooked = cooked;
        this.raw = raw;
    }

    /**
     * Generate a reference token from an encoded (cooked) representation
     *
     * @param cooked the input
     * @return a token
     * @throws JsonPointerException illegal token (bad encode sequence)
     * @throws NullPointerException null input
     */
    public static ReferenceToken fromCooked(final String cooked)
        throws JsonPointerException
    {
        BUNDLE.checkNotNull(cooked, "nullInput");
        return new ReferenceToken(cooked, asRaw(cooked));
    }

    /**
     * Generate a reference token from a decoded (raw) representation
     *
     * @param raw the input
     * @return a token
     * @throws NullPointerException null input
     */
    public static ReferenceToken fromRaw(final String raw)
    {
        BUNDLE.checkNotNull(raw, "nullInput");
        return new ReferenceToken(asCooked(raw), raw);
    }

    /**
     * Generate a reference token from an integer
     *
     * @param index the integer
     * @return a token
     */
    public static ReferenceToken fromInt(final int index)
    {
        final String s = Integer.toString(index);
        return new ReferenceToken(s, s);
    }

    /**
     * Get the raw representation of that token as a string
     *
     * @return the raw representation (for traversing purposes)
     */
    public String getRaw()
    {
        return raw;
    }

    @Override
    public int hashCode()
    {
        return raw.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final ReferenceToken other = (ReferenceToken) obj;
        return raw.equals(other.raw);
    }

    @Override
    public String toString()
    {
        return cooked;
    }

    /**
     * Decode an encoded token
     *
     * @param cooked the encoded token
     * @return the decoded token
     * @throws JsonPointerException bad encoded representation
     */
    private static String asRaw(final String cooked)
        throws JsonPointerException
    {
        final StringBuilder raw = new StringBuilder(cooked.length());

        final CharBuffer buffer = CharBuffer.wrap(cooked);
        boolean inEscape = false;
        char c;

        while (buffer.hasRemaining()) {
            c = buffer.get();
            if (inEscape) {
                appendEscaped(raw, c);
                inEscape = false;
                continue;
            }
            if (c == ESCAPE) {
                inEscape = true;
                continue;
            }
            raw.append(c);
        }

        if (inEscape)
            throw new JsonPointerException(BUNDLE.getMessage("emptyEscape"));

        return raw.toString();
    }

    /**
     * Append a decoded sequence to a {@link StringBuilder}
     *
     * @param sb the string builder to append to
     * @param c the escaped character
     * @throws JsonPointerException illegal escaped character
     */
    private static void appendEscaped(final StringBuilder sb, final char c)
        throws JsonPointerException
    {
        final int index = ENCODED.indexOf(c);
        if (index == -1)
            throw new JsonPointerException(BUNDLE.getMessage("illegalEscape"));

        sb.append(DECODED.get(index));
    }

    /**
     * Encode a raw token
     *
     * @param raw the raw representation
     * @return the cooked, encoded representation
     */
    private static String asCooked(final String raw)
    {
        final StringBuilder cooked = new StringBuilder(raw.length());

        final CharBuffer buffer = CharBuffer.wrap(raw);
        char c;
        int index;

        while (buffer.hasRemaining()) {
            c = buffer.get();
            index = DECODED.indexOf(c);
            if (index != -1)
                cooked.append('~').append(ENCODED.get(index));
            else
                cooked.append(c);
        }

        return cooked.toString();
    }
}
