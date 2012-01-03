package org.eclipse.jetty.npn;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class Transformer implements ClassFileTransformer
{
    private boolean dump;

    public Transformer(Map<String, String> options)
    {
        dump = Boolean.parseBoolean(options.get("dump"));
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException
    {
        switch (className)
        {
            case "sun/security/ssl/Handshaker":
                return customizeHandshaker(classBytes);
            case "sun/security/ssl/ClientHandshaker":
                return customizeClientHandshaker(classBytes);
            case "sun/security/ssl/ServerHandshaker":
                return customizeServerHandshaker(classBytes);
            case "sun/security/ssl/ExtensionType":
                return customizeExtensionType(classBytes);
            case "sun/security/ssl/HelloExtensions":
                return customizeHelloExtensions(classBytes);
        }
        return classBytes;
    }

    private byte[] customizeHandshaker(byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new HandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
//        if (dump)
//            new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), ClassReader.SKIP_DEBUG);
        return bytes;
    }

    private byte[] customizeClientHandshaker(byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ClientHandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
        if (dump)
            new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), ClassReader.SKIP_DEBUG);
        return bytes;
    }

    private byte[] customizeServerHandshaker(byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ServerHandshakerTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
//        if (dump)
//            new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), ClassReader.SKIP_DEBUG);
        return bytes;
    }

    private byte[] customizeExtensionType(byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new ExtensionTypeTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
//        if (dump)
//            new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), ClassReader.SKIP_DEBUG);
        return bytes;
    }

    private byte[] customizeHelloExtensions(byte[] classBytes)
    {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        reader.accept(new HelloExtensionsTransformer(writer), 0);
        byte[] bytes = writer.toByteArray();
//        if (dump)
//            new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), ClassReader.SKIP_DEBUG);
        return bytes;
    }
}
