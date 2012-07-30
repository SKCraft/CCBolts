package com.sk89q.custombolts;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ComputerClassAdapter extends ClassVisitor {
    
    public ComputerClassAdapter(ClassVisitor visitor) {
        super(Opcodes.ASM4, visitor);
    }

    @Override
    public MethodVisitor visitMethod(int version, String name,
            String descriptor, String signature, String[] exceptions) {
        if (name.equalsIgnoreCase("initLua")) {
            return new LuaInitMethodAdapter(super.visitMethod(
                    version, name, descriptor, signature, exceptions));
        }

        return super.visitMethod(version, name, descriptor, signature,
                exceptions);
    }

    private static class LuaInitMethodAdapter extends MethodVisitor {
        private boolean injected = false;

        public LuaInitMethodAdapter(MethodVisitor visitor) {
            super(Opcodes.ASM4, visitor);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.MONITORENTER && !injected) {
                CodeInjector.logger.info("CustomBolts: Installing CCHook.setupGlobals()");
                Label newLabel = new Label();
                super.visitLabel(newLabel);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 1);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/sk89q/custombolts/cc/CCHook", "setupGlobals",
                        "(Ldan200/computer/core/Computer;Lorg/luaj/vm2/LuaTable;)V");
                injected = true;
            }

            super.visitInsn(opcode);
        }
    }
    
}