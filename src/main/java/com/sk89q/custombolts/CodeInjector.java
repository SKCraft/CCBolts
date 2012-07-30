package com.sk89q.custombolts;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class CodeInjector implements ClassFileTransformer {

    static Logger logger = Logger.getLogger(CodeInjector.class
            .getCanonicalName());

    public static void premain(String agentArguments,
            Instrumentation instrumentation) {
        instrumentation.addTransformer(new CodeInjector());
    }

    public byte[] transform(ClassLoader loader, String className,
            Class<?> redefiningClass, ProtectionDomain domain, byte[] bytes)
            throws IllegalClassFormatException {

        try {
            if (className.equals("dan200/computer/core/Computer")) {
                ClassReader reader = new ClassReader(bytes);
                ClassWriter writer = new ClassWriter(reader, 0);
                reader.accept(new ComputerClassAdapter(writer), 0);
                return writer.toByteArray();
            } else if (className.equals("dan200/computer/shared/BlockComputerBase")) {
                ClassReader reader = new ClassReader(bytes);
                ClassWriter writer = new ClassWriter(reader, 0);
                reader.accept(new BlockComputerBaseClassAdapter(writer), 0);
                return writer.toByteArray();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return bytes;
    }
}
