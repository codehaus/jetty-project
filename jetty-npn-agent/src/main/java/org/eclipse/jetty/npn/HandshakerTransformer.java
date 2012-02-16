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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HandshakerTransformer extends ClassVisitor implements Opcodes
{
    public HandshakerTransformer(ClassVisitor visitor)
    {
        super(ASM4, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);

        super.visitInnerClass("org/eclipse/jetty/npn/NextProtoNego$Provider", "org/eclipse/jetty/npn/NextProtoNego", "Provider", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        // Creates an empty sendNextProtocol() method, overridden only by ClientHandshaker
        MethodVisitor mv = super.visitMethod(0, "sendNextProtocol", "(Lorg/eclipse/jetty/npn/NextProtoNego$Provider;)V", null, new String[]{"java/io/IOException"});
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 2);
        mv.visitEnd();

        // Creates an empty updateFinished() method, overridden only by ClientHandshaker
        mv = super.visitMethod(0, "updateFinished", "(Lsun/security/ssl/HandshakeMessage$Finished;)Lsun/security/ssl/HandshakeMessage$Finished;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("sendChangeCipherSpec".equals(name) &&
                "(Lsun/security/ssl/HandshakeMessage$Finished;Z)V".equals(desc))
            return new SendChangeCipherSpecTransformer(visitor);
        return visitor;
    }

    private class SendChangeCipherSpecTransformer extends MethodVisitor implements Opcodes
    {
        public SendChangeCipherSpecTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc)
        {
            // Add the code needed to call sendNextProtocol() and updateFinished()
            super.visitMethodInsn(opcode, owner, name, desc);
            if (opcode == INVOKEVIRTUAL &&
                    "sun/security/ssl/SSLSocketImpl".equals(owner) &&
                    "changeWriteCiphers".equals(name) &&
                    "()V".equals(desc))
            {
                /**
                 * sendNextProtocol(NextProtoNego.get(conn));
                 * mesg = updateFinished(mesg);
                 */
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/Handshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/Handshaker", "sendNextProtocol", "(Lorg/eclipse/jetty/npn/NextProtoNego$Provider;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 1);
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/Handshaker", "updateFinished", "(Lsun/security/ssl/HandshakeMessage$Finished;)Lsun/security/ssl/HandshakeMessage$Finished;");
                super.visitVarInsn(ASTORE, 1);
            }
            else if (opcode == INVOKEVIRTUAL &&
                "sun/security/ssl/SSLEngineImpl".equals(owner) &&
                "changeWriteCiphers".equals(name) &&
                "()V".equals(desc))
            {
                /**
                 * sendNextProtocol(NextProtoNego.get(engine));
                 * mesg = updateFinished(mesg);
                 */
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/Handshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/Handshaker", "sendNextProtocol", "(Lorg/eclipse/jetty/npn/NextProtoNego$Provider;)V");
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 1);
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/Handshaker", "updateFinished", "(Lsun/security/ssl/HandshakeMessage$Finished;)Lsun/security/ssl/HandshakeMessage$Finished;");
                super.visitVarInsn(ASTORE, 1);
            }
        }
    }
}
