package org.eclipse.jetty.npn;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HelloExtensionsTransformer extends ClassVisitor
{
    public HelloExtensionsTransformer(ClassVisitor visitor)
    {
        super(Opcodes.ASM4, visitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("<init>".equals(name) && "(Lsun/security/ssl/HandshakeInStream;)V".equals(desc))
            return new ConstructorTransformer(visitor);
        return visitor;
    }

    private class ConstructorTransformer extends MethodVisitor implements Opcodes
    {
        private Label ifElseExit;

        public ConstructorTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label)
        {
            // We need to get the label that points to the exit of the
            // chained if-elseif-else statements. It turns out that the
            // first GOTO points to that label, so we save it to use it later.
            super.visitJumpInsn(opcode, label);
            if (opcode == GOTO && ifElseExit == null)
                ifElseExit = label;
        }

        @Override
        public void visitTypeInsn(int opcode, String type)
        {
            if (opcode == NEW && "sun/security/ssl/UnknownExtension".equals(type) && ifElseExit != null)
            {
                // Just before the else statement, we inject another elseif branch
                /**
                 * else if (extType == ExtensionType.EXT_NEXT_PROTOCOL_NEGOTIATION)
                 *     extension = new NextProtoNegoExtension(s, extlen);
                 */
                super.visitVarInsn(ALOAD, 5);
                super.visitFieldInsn(GETSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
                Label l8 = new Label();
                super.visitJumpInsn(IF_ACMPNE, l8);
                super.visitTypeInsn(NEW, "sun/security/ssl/NextProtoNegoExtension");
                super.visitInsn(DUP);
                super.visitVarInsn(ALOAD, 1);
                super.visitVarInsn(ILOAD, 4);
                super.visitMethodInsn(INVOKESPECIAL, "sun/security/ssl/NextProtoNegoExtension", "<init>", "(Lsun/security/ssl/HandshakeInStream;I)V");
                super.visitVarInsn(ASTORE, 6);
                super.visitJumpInsn(GOTO, ifElseExit);
                super.visitLabel(l8);
            }
            super.visitTypeInsn(opcode, type);
        }
    }
}
