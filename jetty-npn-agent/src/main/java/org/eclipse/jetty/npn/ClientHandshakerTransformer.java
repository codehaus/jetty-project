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
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClientHandshakerTransformer extends ClassVisitor implements Opcodes
{
    public ClientHandshakerTransformer(ClassVisitor visitor)
    {
        super(ASM4, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);

        super.visitInnerClass("org/eclipse/jetty/npn/NextProtoNego$ClientProvider", "org/eclipse/jetty/npn/NextProtoNego", "ClientProvider", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        super.visitInnerClass("org/eclipse/jetty/npn/NextProtoNego$Provider", "org/eclipse/jetty/npn/NextProtoNego", "Provider", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        // Add a protocols field, to remember the protocols sent by the server
        FieldVisitor protocols = super.visitField(ACC_PRIVATE, "protocols", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", null);
        protocols.visitEnd();

        // Override sendNextProtocol(), writing the NextProtocolMessage to the server
        /**
         * if (provider != null)
         * {
         *     String protocol = ((NextProtoNego.ClientProvider)provider).selectProtocol(protocols);
         *     if (protocol != null)
         *     {
         *         NextProtocolMessage nextProtocol = new NextProtocolMessage(protocol);
         *         nextProtocol.write(output);
         *         output.flush();
         *     }
         * }
         */
        MethodVisitor sendNextProtocol = super.visitMethod(0, "sendNextProtocol", "(Lorg/eclipse/jetty/npn/NextProtoNego$Provider;)V", null, new String[]{"java/io/IOException"});
        sendNextProtocol.visitCode();
        sendNextProtocol.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        sendNextProtocol.visitJumpInsn(IFNULL, l0);
        sendNextProtocol.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
        Label l1 = new Label();
        sendNextProtocol.visitJumpInsn(IFEQ, l1);
        sendNextProtocol.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        sendNextProtocol.visitTypeInsn(NEW, "java/lang/StringBuilder");
        sendNextProtocol.visitInsn(DUP);
        sendNextProtocol.visitLdcInsn("NPN selecting from ");
        sendNextProtocol.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitLdcInsn(" for ");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l2 = new Label();
        sendNextProtocol.visitJumpInsn(IFNULL, l2);
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l3 = new Label();
        sendNextProtocol.visitJumpInsn(GOTO, l3);
        sendNextProtocol.visitLabel(l2);
        sendNextProtocol.visitFrame(Opcodes.F_FULL, 2, new Object[] {"sun/security/ssl/ClientHandshaker", "org/eclipse/jetty/npn/NextProtoNego$Provider"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
        sendNextProtocol.visitLabel(l3);
        sendNextProtocol.visitFrame(Opcodes.F_FULL, 2, new Object[] {"sun/security/ssl/ClientHandshaker", "org/eclipse/jetty/npn/NextProtoNego$Provider"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
        sendNextProtocol.visitLabel(l1);
        sendNextProtocol.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        sendNextProtocol.visitVarInsn(ALOAD, 1);
        sendNextProtocol.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
        sendNextProtocol.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider", "selectProtocol", "(Ljava/util/List;)Ljava/lang/String;");
        sendNextProtocol.visitVarInsn(ASTORE, 2);
        sendNextProtocol.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
        Label l4 = new Label();
        sendNextProtocol.visitJumpInsn(IFEQ, l4);
        sendNextProtocol.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        sendNextProtocol.visitTypeInsn(NEW, "java/lang/StringBuilder");
        sendNextProtocol.visitInsn(DUP);
        sendNextProtocol.visitLdcInsn("NPN selected '");
        sendNextProtocol.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        sendNextProtocol.visitVarInsn(ALOAD, 2);
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitLdcInsn("' for ");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l5 = new Label();
        sendNextProtocol.visitJumpInsn(IFNULL, l5);
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l6 = new Label();
        sendNextProtocol.visitJumpInsn(GOTO, l6);
        sendNextProtocol.visitLabel(l5);
        sendNextProtocol.visitFrame(Opcodes.F_FULL, 3, new Object[] {"sun/security/ssl/ClientHandshaker", "org/eclipse/jetty/npn/NextProtoNego$Provider", "java/lang/String"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
        sendNextProtocol.visitLabel(l6);
        sendNextProtocol.visitFrame(Opcodes.F_FULL, 3, new Object[] {"sun/security/ssl/ClientHandshaker", "org/eclipse/jetty/npn/NextProtoNego$Provider", "java/lang/String"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
        sendNextProtocol.visitLabel(l4);
        sendNextProtocol.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        sendNextProtocol.visitVarInsn(ALOAD, 2);
        sendNextProtocol.visitJumpInsn(IFNULL, l0);
        sendNextProtocol.visitTypeInsn(NEW, "sun/security/ssl/NextProtocolMessage");
        sendNextProtocol.visitInsn(DUP);
        sendNextProtocol.visitVarInsn(ALOAD, 2);
        sendNextProtocol.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtocolMessage", "<init>", "(Ljava/lang/String;)V");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "output", "Lsun/security/ssl/HandshakeOutStream;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtocolMessage", "write", "(Lsun/security/ssl/HandshakeOutStream;)V");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "output", "Lsun/security/ssl/HandshakeOutStream;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HandshakeOutStream", "flush", "()V");
        sendNextProtocol.visitLabel(l0);
        sendNextProtocol.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
        sendNextProtocol.visitInsn(RETURN);
        sendNextProtocol.visitMaxs(4, 3);
        sendNextProtocol.visitEnd();

        // Override updateFinished(), recreating the Finished message after the NextProtocolMessage has been sent
        /**
         * return new Finished(protocolVersion, handshakeHash, Finished.CLIENT, session.getMasterSecret(), cipherSuite);
         */
        MethodVisitor updateFinished = super.visitMethod(0, "updateFinished", "(Lsun/security/ssl/HandshakeMessage$Finished;)Lsun/security/ssl/HandshakeMessage$Finished;", null, null);
        updateFinished.visitCode();
        updateFinished.visitTypeInsn(NEW, "sun/security/ssl/HandshakeMessage$Finished");
        updateFinished.visitInsn(DUP);
        updateFinished.visitVarInsn(ALOAD, 0);
        updateFinished.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocolVersion", "Lsun/security/ssl/ProtocolVersion;");
        updateFinished.visitVarInsn(ALOAD, 0);
        updateFinished.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "handshakeHash", "Lsun/security/ssl/HandshakeHash;");
        updateFinished.visitInsn(ICONST_1);
        updateFinished.visitVarInsn(ALOAD, 0);
        updateFinished.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "session", "Lsun/security/ssl/SSLSessionImpl;");
        updateFinished.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/SSLSessionImpl", "getMasterSecret", "()Ljavax/crypto/SecretKey;");
        updateFinished.visitVarInsn(ALOAD, 0);
        updateFinished.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "cipherSuite", "Lsun/security/ssl/CipherSuite;");
        updateFinished.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/HandshakeMessage$Finished", "<init>", "(Lsun/security/ssl/ProtocolVersion;Lsun/security/ssl/HandshakeHash;ILjavax/crypto/SecretKey;Lsun/security/ssl/CipherSuite;)V");
        updateFinished.visitInsn(ARETURN);
        updateFinished.visitMaxs(7, 2);
        updateFinished.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("getKickstartMessage".equals(name))
            return new GetKickstartMessageTransformer(methodVisitor);
        else if ("serverHello".equals(name))
            return new ServerHelloTransformer(methodVisitor);
        return methodVisitor;
    }

    /**
     * Transforms ClientHandshaker.getKickstartMessage() in order to add the NPN extension
     * at the end of the method.
     */
    private class GetKickstartMessageTransformer extends MethodVisitor implements Opcodes
    {
        private GetKickstartMessageTransformer(MethodVisitor methodVisitor)
        {
            super(ASM4, methodVisitor);
        }

        @Override
        public void visitInsn(int opcode)
        {
            if (opcode == ARETURN)
            {
                /**
                 * NextProtoNego.ClientProvider provider = this.conn != null ?
                 *         (NextProtoNego.ClientProvider)NextProtoNego.get(this.conn) :
                 *         (NextProtoNego.ClientProvider)NextProtoNego.get(this.engine);
                 * if (provider != null && provider.supports())
                 *     clientHelloMessage.extensions.add(new NextProtoNegoExtension());
                 */
                super.visitInsn(POP);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                Label l29 = new Label();
                super.visitJumpInsn(IFEQ, l29);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN present? for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l30 = new Label();
                super.visitJumpInsn(IFNULL, l30);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l31 = new Label();
                super.visitJumpInsn(GOTO, l31);
                super.visitLabel(l30);
                super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l31);
                super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l29);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l32 = new Label();
                super.visitJumpInsn(IFNULL, l32);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
                Label l33 = new Label();
                super.visitJumpInsn(GOTO, l33);
                super.visitLabel(l32);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
                super.visitLabel(l33);
                super.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"org/eclipse/jetty/npn/NextProtoNego$ClientProvider"});
                super.visitVarInsn(ASTORE, 5);
                super.visitVarInsn(ALOAD, 5);
                Label l34 = new Label();
                super.visitJumpInsn(IFNULL, l34);
                super.visitVarInsn(ALOAD, 5);
                super.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider", "supports", "()Z");
                Label l35 = new Label();
                super.visitJumpInsn(IFEQ, l35);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                Label l36 = new Label();
                super.visitJumpInsn(IFEQ, l36);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN supported for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l37 = new Label();
                super.visitJumpInsn(IFNULL, l37);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l38 = new Label();
                super.visitJumpInsn(GOTO, l38);
                super.visitLabel(l37);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l38);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l36);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 4);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ClientHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtoNegoExtension");
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtoNegoExtension", "<init>", "()V");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "add", "(Lsun/security/ssl/HelloExtension;)V");
                Label l39 = new Label();
                super.visitJumpInsn(GOTO, l39);
                super.visitLabel(l35);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                super.visitJumpInsn(IFEQ, l39);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN not supported for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l40 = new Label();
                super.visitJumpInsn(IFNULL, l40);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l41 = new Label();
                super.visitJumpInsn(GOTO, l41);
                super.visitLabel(l40);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l41);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitJumpInsn(GOTO, l39);
                super.visitLabel(l34);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                super.visitJumpInsn(IFEQ, l39);
                super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                super.visitInsn(DUP);
                super.visitLdcInsn("NPN not present for ");
                super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l42 = new Label();
                super.visitJumpInsn(IFNULL, l42);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l43 = new Label();
                super.visitJumpInsn(GOTO, l43);
                super.visitLabel(l42);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitLabel(l43);
                super.visitFrame(Opcodes.F_FULL, 6, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/SessionId", "sun/security/ssl/CipherSuiteList", Opcodes.INTEGER, "sun/security/ssl/HandshakeMessage$ClientHello", "org/eclipse/jetty/npn/NextProtoNego$ClientProvider"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                super.visitLabel(l39);
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitVarInsn(ALOAD, 4);
            }
            super.visitInsn(opcode);
        }
    }

    /**
     * Transforms ClientHandshaker.serverHello() because it checks the
     * extensions sent by the server and throws if it does not recognize one.
     * Also, stores the protocols sent by the server into the protocol field.
     */
    private class ServerHelloTransformer extends MethodVisitor implements Opcodes
    {
        private boolean seenServerNameExtension;
        private int returns;

        private ServerHelloTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc)
        {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == GETSTATIC &&
                    "sun/security/ssl/ExtensionType".equals(owner) &&
                    "EXT_SERVER_NAME".equals(name) &&
                    "Lsun/security/ssl/ExtensionType;".equals(desc))
            {
                seenServerNameExtension = true;
            }
        }

        @Override
        public void visitJumpInsn(int opcode, Label label)
        {
            super.visitJumpInsn(opcode, label);
            if (opcode == IF_ACMPEQ && seenServerNameExtension)
            {
                // Avoid multiple code additions
                seenServerNameExtension = false;

                // Add the NPN extension check
                /**
                 * if (... && (type != ExtensionType.EXT_NEXT_PROTOCOL_NEGOTIATION))
                 */
                super.visitVarInsn(ALOAD, 6);
                super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                super.visitJumpInsn(IF_ACMPEQ, label);
            }
        }

        @Override
        public void visitInsn(int opcode)
        {
            if (opcode == RETURN)
            {
                ++returns;
                if (returns == 2)
                {
                    // Store the protocols sent by the server into the
                    // protocol field just before the end of the method
                    /**
                     * NextProtoNegoExtension npnExt = (NextProtoNegoExtension)mesg.extensions.get(ExtensionType.EXT_NEXT_PROTOCOL_NEGOTIATION);
                     * if (npnExt != null)
                     *     this.protocols = npnExt.getProtocols();
                     */
                    super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                    Label l34 = new Label();
                    super.visitJumpInsn(IFEQ, l34);
                    super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    super.visitInsn(DUP);
                    super.visitLdcInsn("NPN protocols sent by server? for ");
                    super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l35 = new Label();
                    super.visitJumpInsn(IFNULL, l35);
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l36 = new Label();
                    super.visitJumpInsn(GOTO, l36);
                    super.visitLabel(l35);
                    super.visitFrame(Opcodes.F_FULL, 4, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                    super.visitLabel(l36);
                    super.visitFrame(Opcodes.F_FULL, 4, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                    super.visitLabel(l34);
                    super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    super.visitVarInsn(ALOAD, 1);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ServerHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                    super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "get", "(Lsun/security/ssl/ExtensionType;)Lsun/security/ssl/HelloExtension;");
                    super.visitTypeInsn(CHECKCAST, "sun/security/ssl/NextProtoNegoExtension");
                    super.visitVarInsn(ASTORE, 4);
                    super.visitVarInsn(ALOAD, 4);
                    Label l37 = new Label();
                    super.visitJumpInsn(IFNULL, l37);
                    super.visitVarInsn(ALOAD, 0);
                    super.visitVarInsn(ALOAD, 4);
                    super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtoNegoExtension", "getProtocols", "()Ljava/util/List;");
                    super.visitFieldInsn(PUTFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
                    super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                    Label l38 = new Label();
                    super.visitJumpInsn(IFEQ, l38);
                    super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    super.visitInsn(DUP);
                    super.visitLdcInsn("NPN protocols ");
                    super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                    super.visitLdcInsn(" sent by server for ");
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l39 = new Label();
                    super.visitJumpInsn(IFNULL, l39);
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l40 = new Label();
                    super.visitJumpInsn(GOTO, l40);
                    super.visitLabel(l39);
                    super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/NextProtoNegoExtension"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                    super.visitLabel(l40);
                    super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/NextProtoNegoExtension"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                    super.visitJumpInsn(GOTO, l38);
                    super.visitLabel(l37);
                    super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    super.visitFieldInsn(GETSTATIC, "org/eclipse/jetty/npn/NextProtoNego", "debug", "Z");
                    super.visitJumpInsn(IFEQ, l38);
                    super.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
                    super.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    super.visitInsn(DUP);
                    super.visitLdcInsn("NPN protocols not sent by server for ");
                    super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l41 = new Label();
                    super.visitJumpInsn(IFNULL, l41);
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                    Label l42 = new Label();
                    super.visitJumpInsn(GOTO, l42);
                    super.visitLabel(l41);
                    super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/NextProtoNegoExtension"}, 2, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder"});
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                    super.visitLabel(l42);
                    super.visitFrame(Opcodes.F_FULL, 5, new Object[] {"sun/security/ssl/ClientHandshaker", "sun/security/ssl/HandshakeMessage$ServerHello", "sun/security/ssl/ProtocolVersion", "sun/security/ssl/RenegotiationInfoExtension", "sun/security/ssl/NextProtoNegoExtension"}, 3, new Object[] {"java/io/PrintStream", "java/lang/StringBuilder", "java/lang/Object"});
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
                    super.visitLabel(l38);
                    super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                }
            }
            super.visitInsn(opcode);
        }
    }
}
