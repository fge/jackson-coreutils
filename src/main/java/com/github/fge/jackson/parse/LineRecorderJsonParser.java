/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jackson.parse;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public final class LineRecorderJsonParser
    extends JsonParserDelegate
{
    private final Map<JsonPointer, Integer> lines = Maps.newHashMap();
    private JsonPointer ptr = JsonPointer.empty();
    private boolean seenRoot = false;

    public LineRecorderJsonParser(final JsonParser d)
    {
        super(d);
    }

    @Override
    public JsonToken nextToken()
        throws IOException, JsonParseException
    {
        final JsonToken token = super.nextToken();
        final JsonStreamContext context = getParsingContext();
        final JsonLocation location = getCurrentLocation();
        processLineEntry(token, location, context);
        return token;
    }

    private void processLineEntry(final JsonToken token,
        final JsonLocation location, final JsonStreamContext context)
    {
        /*
         * Root needs to be handled specially.
         */
        if (!seenRoot) {
            final int line = location.getLineNr();
            System.out.printf("ENTRY: \"%s\" -> %d\n", ptr, line);
            lines.put(ptr, line);
            seenRoot = true;
            return;
        }

        /*
         * We get that if JSON Pointer "" points to a container... We need to
         * skip that
         */
        if (context.inRoot())
            return;

        /*
         * If the end of a container, "pop" one level
         */
        if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
            ptr = ptr.parent();
            return;
        }

        /*
         * This is not addressable...
         */
        if (token == JsonToken.FIELD_NAME)
            return;

        final JsonStreamContext parent = context.getParent();
        final int line = location.getLineNr();

        /*
         * But this is; however we need to know what the parent is to do things
         * correctly, delegate to another method
         */
        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            startContainer(parent, line);
            return;
        }

        /*
         * OK, "normal" entry, build the pointer
         */
        final JsonPointer entryPointer;

        if (context.inArray())
            entryPointer = ptr.append(context.getCurrentIndex());
        else
            entryPointer = ptr.append(context.getCurrentName());

        System.out.printf("ENTRY: \"%s\" -> %d\n", entryPointer, line);
        lines.put(entryPointer, line);
    }

    private void startContainer(final JsonStreamContext parent, final int line)
    {
        if (parent.inArray())
            ptr = ptr.append(parent.getCurrentIndex());
        else if (parent.inObject())
            ptr = ptr.append(parent.getCurrentName());
        System.out.printf("ENTRY: \"%s\" -> %d\n", ptr, line);
        lines.put(ptr, line);
    }
}
