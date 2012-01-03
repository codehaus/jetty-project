package org.eclipse.jetty.npn;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ExtensionTypeTransformer extends ClassVisitor implements Opcodes
{
    public ExtensionTypeTransformer(ClassVisitor visitor)
    {
        super(ASM4, visitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);

        // Add the static final field EXT_NEXT_PROTOCOL_NEGOTIATION
        FieldVisitor visitor = super.visitField(ACC_FINAL + ACC_STATIC, "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;", null, null);
        visitor.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("<clinit>".equals(name))
            return new StaticInitializerTransformer(visitor);
        return visitor;
    }

    private class StaticInitializerTransformer extends MethodVisitor implements Opcodes
    {
        private StaticInitializerTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitInsn(int opcode)
        {
            if (opcode == RETURN)
            {
                // Initialize the static final field we have added
                super.visitIntInsn(SIPUSH, 0x3374);
                super.visitLdcInsn("next_protocol_negotiation");
                super.visitMethodInsn(INVOKESTATIC, "sun/security/ssl/ExtensionType", "e", "(ILjava/lang/String;)Lsun/security/ssl/ExtensionType;");
                super.visitFieldInsn(PUTSTATIC, "sun/security/ssl/ExtensionType", "EXT_NEXT_PROTOCOL_NEGOTIATION", "Lsun/security/ssl/ExtensionType;");
            }
            super.visitInsn(opcode);
        }
    }
}
