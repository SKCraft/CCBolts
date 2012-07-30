package com.sk89q.custombolts;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class BlockComputerBaseClassAdapter extends ClassVisitor {

    public BlockComputerBaseClassAdapter(ClassVisitor visitor) {
        super(Opcodes.ASM4, visitor);
    }

    @Override
    public MethodVisitor visitMethod(int version, String name,
            String descriptor, String signature, String[] exceptions) {
        if (name.equalsIgnoreCase("getPeripheralAt")) {
            return new GetPeriphrealAtMethodAdapter(super.visitMethod(version,
                    name, descriptor, signature, exceptions));
        }

        return super.visitMethod(version, name, descriptor, signature,
                exceptions);
    }

    private static class GetPeriphrealAtMethodAdapter extends MethodVisitor {
        private boolean injected = false;
        private boolean foundFirstReturn = false;

        public GetPeriphrealAtMethodAdapter(MethodVisitor visitor) {
            super(Opcodes.ASM4, visitor);
        }

        @Override
        public void visitInsn(int opcode) {
            CodeInjector.logger.info("CustomBolts: " + opcode);
            if (opcode == Opcodes.ARETURN) {
                foundFirstReturn = true;
            } else if (opcode == Opcodes.ACONST_NULL && !injected && foundFirstReturn) {
                CodeInjector.logger
                        .info("CustomBolts: Installing CCHook.getPeripheral()");
                Label newLabel = new Label();
                super.visitLabel(newLabel);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ILOAD, 1);
                super.visitVarInsn(Opcodes.ILOAD, 2);
                super.visitVarInsn(Opcodes.ILOAD, 3);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/sk89q/custombolts/cc/CCHook", "getPeripheral",
                        "(Lnet/minecraft/server/World;III)Ldan200/computer/api/IPeripheral;");
                super.visitInsn(Opcodes.ARETURN);
                injected = true;
            }
            
            super.visitInsn(opcode);
        }
    }

}