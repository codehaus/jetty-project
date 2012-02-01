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

package org.eclipse.jetty.npn;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class Transformer implements ClassFileTransformer
{
    private String dump;

    public Transformer(Map<String, String> options)
    {
        dump = options.get("dump");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException
    {
        try
        {
            switch (className)
            {
                case "sun/security/ssl/Handshaker":
                    return customizeHandshaker(className, classBytes);
                case "sun/security/ssl/ClientHandshaker":
                    return customizeClientHandshaker(className, classBytes);
                case "sun/security/ssl/ServerHandshaker":
                    return customizeServerHandshaker(className, classBytes);
                case "sun/security/ssl/ExtensionType":
                    return customizeExtensionType(className, classBytes);
                case "sun/security/ssl/HelloExtensions":
                    return customizeHelloExtensions(className, classBytes);
                case "sun/security/ssl/SSLEngineImpl":
                    return customizeSSLEngineImpl(className, classBytes);
            }
            return classBytes;
        }
        catch (Throwable x)
        {
            System.err.println("WARNING: could not transform class " + className + " NPN support is now broken");
            x.printStackTrace();
            return classBytes;
        }
    }

    private byte[] customizeHandshaker(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new HandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private byte[] customizeClientHandshaker(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ClientHandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private byte[] customizeServerHandshaker(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ServerHandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private byte[] customizeExtensionType(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ExtensionTypeTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private byte[] customizeHelloExtensions(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new HelloExtensionsTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private byte[] customizeSSLEngineImpl(String className, byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
        {
            // While computing maxs and frames, ASM tries to load SSLEngineImpl
            // and this throws a ClassCircularityError because it's trying to
            // load a class that is being transformed; so we override
            // getCommonSuperClass() to avoid to load SSLEngineImpl.
            @Override
            protected String getCommonSuperClass(String type1, String type2)
            {
                if ("sun/security/ssl/SSLEngineImpl".equals(type1) && "java/lang/Object".equals(type2))
                    return type2;
                return super.getCommonSuperClass(type1, type2);
            }
        };
        reader.accept(new SSLEngineImplTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        dump(className, classBytes, bytes);
        return bytes;
    }

    private synchronized void dump(String className, byte[] originalBytes, byte[] transformedBytes)
    {
        try
        {
            if (dump == null)
                return;

            Path dumpDir = Paths.get(dump);
            if (!Files.exists(dumpDir))
                Files.createDirectories(dumpDir);

            String fileName = className.replaceAll("/", ".");

            Path originalPath = Files.createFile(dumpDir.resolve(fileName + "-original.java"));
            Writer output = new StringWriter();
            new ClassReader(originalBytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(output)), ClassReader.SKIP_DEBUG);
            Files.write(originalPath, output.toString().getBytes("UTF-8"), StandardOpenOption.CREATE);

            Path transformedPath = Files.createFile(dumpDir.resolve(fileName + "-transformed.java"));
            output = new StringWriter();
            new ClassReader(transformedBytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(output)), ClassReader.SKIP_DEBUG);
            Files.write(transformedPath, output.toString().getBytes("UTF-8"), StandardOpenOption.CREATE);
        }
        catch (IOException x)
        {
            System.err.println("Failed to dump class " + className + " to directory " + dump);
            x.printStackTrace();
        }
    }
}
