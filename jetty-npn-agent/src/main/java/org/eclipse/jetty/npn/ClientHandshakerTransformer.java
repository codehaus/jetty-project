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

        // Add a protocol field, to remember the protocols sent by the server
        FieldVisitor protocols = super.visitField(ACC_PRIVATE, "protocols", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", null);
        protocols.visitEnd();

        // Override sendNextProtocol(), writing the NextProtocolMessage to the server
        /**
         * if (provider != null && protocols != null)
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
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
        sendNextProtocol.visitJumpInsn(IFNULL, l0);
        sendNextProtocol.visitVarInsn(ALOAD, 1);
        sendNextProtocol.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
        sendNextProtocol.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider", "selectProtocol", "(Ljava/util/List;)Ljava/lang/String;");
        sendNextProtocol.visitVarInsn(ASTORE, 2);
        sendNextProtocol.visitVarInsn(ALOAD, 2);
        sendNextProtocol.visitJumpInsn(IFNULL, l0);
        sendNextProtocol.visitTypeInsn(NEW, "sun/security/ssl/NextProtocolMessage");
        sendNextProtocol.visitInsn(DUP);
        sendNextProtocol.visitVarInsn(ALOAD, 2);
        sendNextProtocol.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtocolMessage", "<init>", "(Ljava/lang/String;)V");
        sendNextProtocol.visitVarInsn(ASTORE, 3);
        sendNextProtocol.visitVarInsn(ALOAD, 3);
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "output", "Lsun/security/ssl/HandshakeOutStream;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtocolMessage", "write", "(Lsun/security/ssl/HandshakeOutStream;)V");
        sendNextProtocol.visitVarInsn(ALOAD, 0);
        sendNextProtocol.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "output", "Lsun/security/ssl/HandshakeOutStream;");
        sendNextProtocol.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HandshakeOutStream", "flush", "()V");
        sendNextProtocol.visitLabel(l0);
        sendNextProtocol.visitInsn(RETURN);
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
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l29 = new Label();
                super.visitJumpInsn(IFNULL, l29);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
                Label l30 = new Label();
                super.visitJumpInsn(GOTO, l30);
                super.visitLabel(l29);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ClientHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider");
                super.visitLabel(l30);
                super.visitVarInsn(ASTORE, 5);
                super.visitVarInsn(ALOAD, 5);
                Label l31 = new Label();
                super.visitJumpInsn(IFNULL, l31);
                super.visitVarInsn(ALOAD, 5);
                super.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ClientProvider", "supports", "()Z");
                super.visitJumpInsn(IFEQ, l31);
                super.visitVarInsn(ALOAD, 4);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ClientHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtoNegoExtension");
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtoNegoExtension", "<init>", "()V");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "add", "(Lsun/security/ssl/HelloExtension;)V");
                super.visitLabel(l31);
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
                    super.visitVarInsn(ALOAD, 1);
                    super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ServerHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                    super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                    super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "get", "(Lsun/security/ssl/ExtensionType;)Lsun/security/ssl/HelloExtension;");
                    super.visitTypeInsn(CHECKCAST, "sun/security/ssl/NextProtoNegoExtension");
                    super.visitVarInsn(ASTORE, 4);
                    super.visitVarInsn(ALOAD, 4);
                    Label l34 = new Label();
                    super.visitJumpInsn(IFNULL, l34);
                    super.visitVarInsn(ALOAD, 0);
                    super.visitVarInsn(ALOAD, 4);
                    super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtoNegoExtension", "getProtocols", "()Ljava/util/List;");
                    super.visitFieldInsn(PUTFIELD, "sun/security/ssl/ClientHandshaker", "protocols", "Ljava/util/List;");
                    super.visitLabel(l34);
                }
            }
            super.visitInsn(opcode);
        }
    }
}
