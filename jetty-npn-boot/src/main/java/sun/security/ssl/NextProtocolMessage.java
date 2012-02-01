/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class NextProtocolMessage extends HandshakeMessage
{
    public static final int ID = 67;

    private final byte[] protocolBytes;
    private final byte[] paddingBytes;
    private final String protocol;

    public NextProtocolMessage(String protocol)
    {
        protocolBytes = protocol.getBytes(Charset.forName("UTF-8"));
        paddingBytes = new byte[32 - ((1 + protocolBytes.length + 1) % 32)];
        this.protocol = protocol;
    }

    public NextProtocolMessage(HandshakeInStream input) throws IOException
    {
        protocolBytes = input.getBytes8();
        paddingBytes = input.getBytes8();
        protocol = new String(protocolBytes, "UTF-8");
    }

    public String getProtocol()
    {
        return protocol;
    }

    @Override
    int messageType()
    {
        return ID;
    }

    @Override
    int messageLength()
    {
        return 1 + protocolBytes.length + 1 + paddingBytes.length;
    }

    @Override
    void send(HandshakeOutStream output) throws IOException
    {
        output.putBytes8(protocolBytes);
        output.putBytes8(paddingBytes);
    }

    @Override
    void print(PrintStream stream) throws IOException
    {
        stream.printf("*** NextProtocol(%s)%n", protocol);
    }
}
