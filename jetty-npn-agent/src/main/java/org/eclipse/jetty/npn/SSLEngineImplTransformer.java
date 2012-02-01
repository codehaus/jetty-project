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

public class SSLEngineImplTransformer extends ClassVisitor implements Opcodes
{
    public SSLEngineImplTransformer(ClassVisitor visitor)
    {
        super(ASM4, visitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if ("readRecord".equals(name))
            return new ReadRecordTransformer(methodVisitor);
        return methodVisitor;
    }

    /**
     * The SSLEngineImpl state machine assumes that after a ChangeCipherSpec there will be
     * a FinishedMessage, but with NPN there is a NextProtocol message and the state machine
     * is messed up. This transformer modifies method readRecord():1102 by removing the
     * assignment "expectingFinished=false" that is done just after "handshaker.process_record()"
     * and by adding it back where the "handshaker" field is nulled out.
     */
    private class ReadRecordTransformer extends MethodVisitor implements Opcodes
    {
        private boolean seenProcessRecord;
        private boolean skippedExpectingFinished;

        public ReadRecordTransformer(MethodVisitor visitor)
        {
            super(ASM4, visitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc)
        {
            super.visitMethodInsn(opcode, owner, name, desc);
            if (opcode == INVOKEVIRTUAL &&
                    "sun/security/ssl/Handshaker".equals(owner) &&
                    "process_record".equals(name))
                seenProcessRecord = true;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc)
        {
            if (seenProcessRecord &&
                    "sun/security/ssl/SSLEngineImpl".equals(owner) &&
                    "expectingFinished".equals(name))
            {
                // We have "this" and the value to assign on
                // the stack, pop them and skip this assignment
                super.visitInsn(POP);
                super.visitInsn(POP);
                seenProcessRecord = false;
                skippedExpectingFinished = true;
                return;
            }

            super.visitFieldInsn(opcode, owner, name, desc);

            if (skippedExpectingFinished &&
                    opcode == PUTFIELD &&
                    "sun/security/ssl/SSLEngineImpl".equals(owner) &&
                    "handshaker".equals(name))
            {
                super.visitVarInsn(ALOAD, 0);
                super.visitInsn(ICONST_0);
                super.visitFieldInsn(PUTFIELD, "sun/security/ssl/SSLEngineImpl", "expectingFinished", "Z");
            }
        }
    }
}
