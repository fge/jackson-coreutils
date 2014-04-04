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
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public final class LineRecorderJsonParser
    extends JsonParserDelegate
{
    private static final Joiner.MapJoiner JOINER
        = Joiner.on("; ").withKeyValueSeparator(": ").useForNull("NULL!");
    private final Map<JsonPointer, Integer> lines = Maps.newHashMap();
    private JsonPointer ptr = JsonPointer.empty();
    private boolean seenRoot = false;
    private final Map<String, Object> info = Maps.newLinkedHashMap();

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
//        info.put("token", token);
//        info.put("line", location.getLineNr());
//        info.put("inRoot", context.inRoot());
//        info.put("inArray", context.inArray());
//        info.put("index", context.getCurrentIndex());
//        info.put("inObject", context.inObject());
//        info.put("memberName", context.getCurrentName());
//        info.put("nrEntries", context.getEntryCount());
//        info.put("type", context.getTypeDesc());
//        JOINER.appendTo(System.out, info);
//        System.out.println();
        processLineEntry(token, location, context);
        return token;
    }

    private void processLineEntry(final JsonToken token,
        final JsonLocation location, final JsonStreamContext context)
    {
        if (!seenRoot) {
            final int line = location.getLineNr();
            System.out.printf("ENTRY: \"%s\" -> %d\n", ptr, line);
            lines.put(ptr, line);
            seenRoot = true;
            return;
        }

        if (context.inRoot())
            return;

        if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
            ptr = ptr.parent();
            return;
        }

        if (token == JsonToken.FIELD_NAME)
            return;

        final JsonStreamContext parent = context.getParent();
        final int line = location.getLineNr();

        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            startContainer(parent, line);
            return;
        }

        final JsonPointer entryPointer;

        if (context.inArray())
            entryPointer = ptr.append(context.getCurrentIndex());
        else
            entryPointer = ptr.append(context.getCurrentName());

        System.out.printf("ENTRY: \"%s\" -> %d\n", entryPointer, line);
        lines.put(entryPointer, line);
    }

    private void handleInObject(final JsonToken token,
        final JsonLocation location, final JsonStreamContext context)
    {
        final JsonStreamContext parent = context.getParent();
        final int line = location.getLineNr();

        if (token == JsonToken.START_OBJECT) {
            startContainer(parent, line);
            return;
        }

        System.out.printf("ENTRY: \"%s\" -> %d\n",
            ptr.append(context.getCurrentName()), line);
        lines.put(ptr.append(context.getCurrentName()), line);
    }

    private void handleInArray(final JsonToken token,
        final JsonLocation location, final JsonStreamContext context)
    {
        final JsonStreamContext parent = context.getParent();
        final int line = location.getLineNr();

        if (token == JsonToken.START_ARRAY) {
            startContainer(parent, line);
            return;
        }

        System.out.printf("ENTRY: \"%s\" -> %d\n",
            ptr.append(context.getCurrentIndex()), line);
        lines.put(ptr.append(context.getCurrentIndex()), line);
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
