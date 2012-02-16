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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ServerHandshakerTransformer extends ClassVisitor implements Opcodes
{
    private static final int NEXT_PROTOCOL_MESSAGE_ID = 67;

    public ServerHandshakerTransformer(ClassVisitor visitor)
    {
        super(ASM4, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);

        super.visitInnerClass("org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "org/eclipse/jetty/npn/NextProtoNego", "ServerProvider", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        super.visitInnerClass("org/eclipse/jetty/npn/NextProtoNego$Provider", "org/eclipse/jetty/npn/NextProtoNego", "Provider", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        // Add a nextProtocol() method to process the NextProtocolMessage sent by the client
        /**
         * NextProtoNego.ServerProvider provider = conn != null ?
         *         (NextProtoNego.ServerProvider)NextProtoNego.get(conn) :
         *         (NextProtoNego.ServerProvider)NextProtoNego.get(engine);
         * if (provider != null)
         *     provider.protocolSelected(message.getProtocol());
         */
        MethodVisitor nextProtocol = super.visitMethod(Opcodes.ACC_PRIVATE, "nextProtocol", "(Lsun/security/ssl/NextProtocolMessage;)V", null, new String[]{"java/io/IOException"});
        nextProtocol.visitCode();
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l0 = new Label();
        nextProtocol.visitJumpInsn(IFNULL, l0);
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        nextProtocol.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
        nextProtocol.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
        Label l1 = new Label();
        nextProtocol.visitJumpInsn(GOTO, l1);
        nextProtocol.visitLabel(l0);
        nextProtocol.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
        nextProtocol.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
        nextProtocol.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
        nextProtocol.visitLabel(l1);
        nextProtocol.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"org/eclipse/jetty/npn/NextProtoNego$ServerProvider"});
        nextProtocol.visitVarInsn(ASTORE, 2);
        nextProtocol.visitVarInsn(ALOAD, 2);
        Label l2 = new Label();
        nextProtocol.visitJumpInsn(IFNULL, l2);
        nextProtocol.visitVarInsn(ALOAD, 1);
        nextProtocol.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtocolMessage", "getProtocol", "()Ljava/lang/String;");
        nextProtocol.visitVarInsn(ASTORE, 3);
        nextProtocol.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
        Label l3 = new Label();
        nextProtocol.visitJumpInsn(IFEQ, l3);
        nextProtocol.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        nextProtocol.visitTypeInsn(NEW, "java/lang/StringBuilder");
        nextProtocol.visitInsn(DUP);
        nextProtocol.visitLdcInsn("NPN next protocol '");
        nextProtocol.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        nextProtocol.visitVarInsn(ALOAD, 3);
        nextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        nextProtocol.visitLdcInsn("' sent by client for ");
        nextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l4 = new Label();
        nextProtocol.visitJumpInsn(IFNULL, l4);
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l5 = new Label();
        nextProtocol.visitJumpInsn(GOTO, l5);
        nextProtocol.visitLabel(l4);
        nextProtocol.visitFrame(Opcodes.F_FULL, 4, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/NextProtocolMessage", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/lang/String"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
        nextProtocol.visitVarInsn(ALOAD, 0);
        nextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
        nextProtocol.visitLabel(l5);
        nextProtocol.visitFrame(Opcodes.F_FULL, 4, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/NextProtocolMessage", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/lang/String"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
        nextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
        nextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
        nextProtocol.visitLabel(l3);
        nextProtocol.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        nextProtocol.visitVarInsn(ALOAD, 2);
        nextProtocol.visitVarInsn(ALOAD, 3);
        nextProtocol.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "protocolSelected", "(Ljava/lang/String;)V");
        nextProtocol.visitLabel(l2);
        nextProtocol.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
        nextProtocol.visitInsn(RETURN);
        nextProtocol.visitMaxs(4, 4);
        nextProtocol.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("clientHello".equals(name))
            return new ClientHelloTransformer(methodVisitor);
        else if ("processMessage".equals(name))
            return new ProcessMessageTransformer(methodVisitor);
        return methodVisitor;
    }

    private class ClientHelloTransformer extends MethodVisitor implements Opcodes
    {
        private ClientHelloTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc)
        {
            // Need to intercept the instruction that writes the ServerHello
            if (opcode == INVOKEVIRTUAL &&
                    "sun/security/ssl/HandshakeMessage$ServerHello".equals(owner) &&
                    "write".equals(name) &&
                    "(Lsun/security/ssl/HandshakeOutStream;)V".equals(desc))
            {
                // At this point on the bytecode stack there are 2 items:
                // the target of the call and the parameter; we pop them
                // to inject our code
                super.visitInsn(POP);
                super.visitInsn(POP);

                /**
                 * if (mesg.extensions.get(ExtensionType.EXT_NEXT_PROTOCOL_NEGOTIATION) != null)
                 * {
                 *     NextProtoNego.ServerProvider provider = conn != null ?
                 *             (NextProtoNego.ServerProvider)NextProtoNego.get(conn) :
                 *             (NextProtoNego.ServerProvider)NextProtoNego.get(engine);
                 *     if (provider != null)
                 *     {
                 *         List<String> protocols = provider.protocols();
                 *         if (protocols != null)
                 *             m1.extensions.add(new NextProtoNegoExtension(protocols));
                 *     }
                 * }
                 */
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                Label l58 = new Label();
                super.visitJumpInsn(IFEQ, l58);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN received? for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l59 = new Label();
                super.visitJumpInsn(IFNULL, l59);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l60 = new Label();
                super.visitJumpInsn(GOTO, l60);
                super.visitLabel(l59);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l60);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l58);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 1);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ClientHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "get", "(Lsun/security/ssl/ExtensionType;)Lsun/security/ssl/HelloExtension;");
                Label l61 = new Label();
                super.visitJumpInsn(IFNULL, l61);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                Label l62 = new Label();
                super.visitJumpInsn(IFEQ, l62);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN received for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l63 = new Label();
                super.visitJumpInsn(IFNULL, l63);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l64 = new Label();
                super.visitJumpInsn(GOTO, l64);
                super.visitLabel(l63);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l64);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l62);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l65 = new Label();
                super.visitJumpInsn(IFNULL, l65);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
                Label l66 = new Label();
                super.visitJumpInsn(GOTO, l66);
                super.visitLabel(l65);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
                super.visitLabel(l66);
                super.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"org/eclipse/jetty/npn/NextProtoNego$ServerProvider"});
                super.visitVarInsn(ASTORE, 7);
                super.visitVarInsn(ALOAD, 7);
                Label l67 = new Label();
                super.visitJumpInsn(IFNULL, l67);
                super.visitVarInsn(ALOAD, 7);
                super.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "protocols", "()Ljava/util/List;");
                super.visitVarInsn(ASTORE, 8);
                super.visitVarInsn(ALOAD, 8);
                Label l68 = new Label();
                super.visitJumpInsn(IFNULL, l68);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                Label l69 = new Label();
                super.visitJumpInsn(IFEQ, l69);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN protocols ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 8);
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitLdcInsn(" sent to client for ");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l70 = new Label();
                super.visitJumpInsn(IFNULL, l70);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l71 = new Label();
                super.visitJumpInsn(GOTO, l71);
                super.visitLabel(l70);
                super.visitFrame(Opcodes.F_FULL, 9, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/util/List"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l71);
                super.visitFrame(Opcodes.F_FULL, 9, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/util/List"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l69);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 5);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ServerHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtoNegoExtension");
                super.visitInsn(DUP);
                super.visitVarInsn(ALOAD, 8);
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtoNegoExtension", "<init>", "(Ljava/util/List;)V");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "add", "(Lsun/security/ssl/HelloExtension;)V");
                Label l72 = new Label();
                super.visitJumpInsn(GOTO, l72);
                super.visitLabel(l68);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                super.visitJumpInsn(IFEQ, l72);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN protocols missing for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l73 = new Label();
                super.visitJumpInsn(IFNULL, l73);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l74 = new Label();
                super.visitJumpInsn(GOTO, l74);
                super.visitLabel(l73);
                super.visitFrame(Opcodes.F_FULL, 9, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/util/List"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l74);
                super.visitFrame(Opcodes.F_FULL, 9, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "java/util/List"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l72);
                super.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
                Label l75 = new Label();
                super.visitJumpInsn(GOTO, l75);
                super.visitLabel(l67);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                super.visitJumpInsn(IFEQ, l75);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN not supported for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l76 = new Label();
                super.visitJumpInsn(IFNULL, l76);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l77 = new Label();
                super.visitJumpInsn(GOTO, l77);
                super.visitLabel(l76);
                super.visitFrame(Opcodes.F_FULL, 8, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l77);
                super.visitFrame(Opcodes.F_FULL, 8, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "org/eclipse/jetty/npn/NextProtoNego$ServerProvider"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l75);
                super.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
                Label l78 = new Label();
                super.visitJumpInsn(GOTO, l78);
                super.visitLabel(l61);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                super.visitJumpInsn(IFEQ, l78);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN not received for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l79 = new Label();
                super.visitJumpInsn(IFNULL, l79);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l80 = new Label();
                super.visitJumpInsn(GOTO, l80);
                super.visitLabel(l79);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l80);
                super.visitFrame(Opcodes.F_FULL, 7, new Object[] {"sun/security/ssl/ServerHandshaker", "sun/security/ssl/HandshakeMessage$ClientHello", Opcodes.INTEGER, "sun/security/ssl/CipherSuiteList", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l78);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                // Re-add the target and the parameter of the call
                super.visitVarInsn(ALOAD, 5);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "output", "Lsun/security/ssl/HandshakeOutStream;");
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    private class ProcessMessageTransformer extends MethodVisitor implements Opcodes
    {
        private Label label;
        private boolean seenSwitch;
        private Label switchExitLabel;
        private boolean seenSwitchExit;

        private ProcessMessageTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
        {
            // We need to add one more case block, so we need one more label
            int[] newKeys = new int[keys.length + 1];
            System.arraycopy(keys, 0, newKeys, 0, keys.length);
            newKeys[keys.length] = NEXT_PROTOCOL_MESSAGE_ID;

            Label[] newLabels = new Label[labels.length + 1];
            System.arraycopy(labels, 0, newLabels, 0, labels.length);
            label = new Label();
            newLabels[labels.length] = label;

            super.visitLookupSwitchInsn(dflt, newKeys, newLabels);

            seenSwitch = true;
        }

        @Override
        public void visitJumpInsn(int opcode, Label label)
        {
            super.visitJumpInsn(opcode, label);
            if (opcode == GOTO && seenSwitch)
            {
                seenSwitch = false;
                switchExitLabel = label;

                // Add the case statement
                /**
                 * case NEXT_PROTOCOL_MESSAGE_ID:
                 *     nextProtocol(new NextProtocolMessage(input));
                 *     break;
                 */
                super.visitLabel(this.label);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 0);
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtocolMessage");
                super.visitInsn(DUP);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "input", "Lsun/security/ssl/HandshakeInStream;");
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtocolMessage", "<init>", "(Lsun/security/ssl/HandshakeInStream;)V");
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/ServerHandshaker", "nextProtocol", "(Lsun/security/ssl/NextProtocolMessage;)V");
                super.visitJumpInsn(GOTO, label);
            }
            else if (opcode == IF_ICMPEQ && seenSwitchExit)
            {
                seenSwitchExit = false;

                // Add additional condition to skip NextProtocol messages
                /**
                 * if (... && type != NEXT_PROTOCOL_MESSAGE_ID)
                 */
                super.visitVarInsn(ILOAD, 1);
                super.visitIntInsn(BIPUSH, NEXT_PROTOCOL_MESSAGE_ID);
                super.visitJumpInsn(IF_ICMPEQ, label);
            }
        }

        @Override
        public void visitLabel(Label label)
        {
            // At the end of the method there is an annoying state update that
            // fails when a NextProtocol message is received so we patch it.
            super.visitLabel(label);
            if (label == switchExitLabel)
                seenSwitchExit = true;
        }
    }
}
