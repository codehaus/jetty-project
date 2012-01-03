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

        // Add a nextProtocol() method to process the NextProtocolMessage sent by the client
        /**
         * NextProtoNego.ServerProvider provider = conn != null ?
         *         (NextProtoNego.ServerProvider)NextProtoNego.get(conn) :
         *         (NextProtoNego.ServerProvider)NextProtoNego.get(engine);
         * if (provider != null)
         *     provider.protocolSelected(message.getProtocol());
         */
        MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PRIVATE, "nextProtocol", "(Lsun/security/ssl/NextProtocolMessage;)V", null, new String[]{"java/io/IOException"});
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        Label l0 = new Label();
        methodVisitor.visitJumpInsn(IFNULL, l0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
        methodVisitor.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
        Label l1 = new Label();
        methodVisitor.visitJumpInsn(GOTO, l1);
        methodVisitor.visitLabel(l0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
        methodVisitor.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
        methodVisitor.visitLabel(l1);
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 2);
        Label l2 = new Label();
        methodVisitor.visitJumpInsn(IFNULL, l2);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/NextProtocolMessage", "getProtocol", "()Ljava/lang/String;");
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "protocolSelected", "(Ljava/lang/String;)V");
        methodVisitor.visitLabel(l2);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitEnd();
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
                super.visitVarInsn(ALOAD, 1);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ClientHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "get", "(Lsun/security/ssl/ExtensionType;)Lsun/security/ssl/HelloExtension;");
                Label l58 = new Label();
                super.visitJumpInsn(IFNULL, l58);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                Label l59 = new Label();
                super.visitJumpInsn(IFNULL, l59);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "conn", "Lsun/security/ssl/SSLSocketImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLSocket;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
                Label l60 = new Label();
                super.visitJumpInsn(GOTO, l60);
                super.visitLabel(l59);
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/ServerHandshaker", "engine", "Lsun/security/ssl/SSLEngineImpl;");
                super.visitMethodInsn(INVOKESTATIC, "org/eclipse/jetty/npn/NextProtoNego", "get", "(Ljavax/net/ssl/SSLEngine;)Lorg/eclipse/jetty/npn/NextProtoNego$Provider;");
                super.visitTypeInsn(CHECKCAST, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider");
                super.visitLabel(l60);
                super.visitVarInsn(ASTORE, 7);
                super.visitVarInsn(ALOAD, 7);
                super.visitJumpInsn(IFNULL, l58);
                super.visitVarInsn(ALOAD, 7);
                super.visitMethodInsn(INVOKEINTERFACE, "org/eclipse/jetty/npn/NextProtoNego$ServerProvider", "protocols", "()Ljava/util/List;");
                super.visitVarInsn(ASTORE, 8);
                super.visitVarInsn(ALOAD, 8);
                super.visitJumpInsn(IFNULL, l58);
                super.visitVarInsn(ALOAD, 5);
                super.visitFieldInsn(GETFIELD, "sun/security/ssl/HandshakeMessage$ServerHello", "extensions", "Lsun/security/ssl/HelloExtensions;");
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtoNegoExtension");
                super.visitInsn(DUP);
                super.visitVarInsn(ALOAD, 8);
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtoNegoExtension", "<init>", "(Ljava/util/List;)V");
                super.visitMethodInsn(INVOKEVIRTUAL, "sun/security/ssl/HelloExtensions", "add", "(Lsun/security/ssl/HelloExtension;)V");
                super.visitLabel(l58);

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
            // We need to add one more case
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
