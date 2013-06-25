package com.github.fge.jackson.jsonpointer;

import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.bundle.PropertiesBundle;
import com.github.fge.msgsimple.serviceloader.MessageBundleProvider;

public final class JsonPointerMessages
    implements MessageBundleProvider
{
    @Override
    public MessageBundle getBundle()
    {
        return PropertiesBundle.forPath("com/github/fge/jackson/jsonpointer");
    }
}
