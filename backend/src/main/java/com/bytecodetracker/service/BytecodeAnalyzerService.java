package com.bytecodetracker.service;

import com.bytecodetracker.model.RiskLevel;
import lombok.Builder;
import lombok.Getter;
import org.objectweb.asm.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BytecodeAnalyzerService {

    public AnalysisSummary analyze(byte[] bytecode) {
        List<MethodRisk> methodRisks = new ArrayList<>();
        List<ViolationRisk> violations = new ArrayList<>();
        final String[] classNameRef = new String[1];

        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                classNameRef[0] = name.replace('/', '.');
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodAccumulator accumulator = new MethodAccumulator(name);
                return new MethodVisitor(Opcodes.ASM9) {

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
                        accumulator.inspectInvoke(opcode, owner, methodName);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String) {
                            accumulator.inspectLdc((String) value);
                        }
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        accumulator.inspectFieldInsn(opcode, owner, name);
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.NEW) {
                            accumulator.inspectType(type);
                        }
                    }

                    @Override
                    public void visitEnd() {
                        MethodRisk methodRisk = accumulator.toRisk();
                        methodRisks.add(methodRisk);
                        if (methodRisk.getRiskLevel() != RiskLevel.LOW) {
                            violations.add(ViolationRisk.builder()
                                    .methodName(methodRisk.getMethodName())
                                    .riskLevel(methodRisk.getRiskLevel())
                                    .reason(methodRisk.getRiskReason())
                                    .build());
                        }
                    }
                };
            }
        }, ClassReader.SKIP_FRAMES);

        int totalMethods = methodRisks.size();
        int dangerousCount = (int) methodRisks.stream().filter(m -> m.getRiskLevel() == RiskLevel.HIGH).count();
        int safeCount = (int) methodRisks.stream().filter(m -> m.getRiskLevel() == RiskLevel.LOW).count();
        RiskLevel overallRisk = dangerousCount > 0
                ? RiskLevel.HIGH
                : (violations.isEmpty() ? RiskLevel.LOW : RiskLevel.MEDIUM);

        return AnalysisSummary.builder()
                .className(classNameRef[0] == null ? "UnknownClass" : classNameRef[0])
                .methodRisks(methodRisks)
                .violations(violations)
                .totalMethods(totalMethods)
                .dangerousCount(dangerousCount)
                .safeCount(safeCount)
                .riskLevel(overallRisk)
                .build();
    }

    private static class MethodAccumulator {
        private final String methodName;
        private RiskLevel riskLevel = RiskLevel.LOW;
        private String reason = "No dangerous opcode pattern detected";

        private MethodAccumulator(String methodName) {
            this.methodName = methodName;
        }

        private void inspectInvoke(int opcode, String owner, String methodName) {
            String key = (owner + "." + methodName).toLowerCase(Locale.ROOT);

            if (key.contains("java/lang/runtime.exec")
                    || key.contains("java/lang/system.exit")
                    || key.contains("java/lang/classloader")
                    || key.contains("java/lang/processbuilder")) {
                setHigh("Detected system execution or dynamic loading call: " + owner + "." + methodName);
                return;
            }

            if (key.contains("java/lang/reflect/method.invoke")) {
                setMedium("Detected reflection invocation: " + owner + "." + methodName);
                return;
            }

            // Class.forName
            if (key.contains("java/lang/class.forname") || (owner.toLowerCase(Locale.ROOT).contains("java/lang/class") && methodName.toLowerCase(Locale.ROOT).contains("forname"))) {
                setMedium("Detected dynamic class loading via Class.forName: " + owner + "." + methodName);
                return;
            }

            // defineClass on classloader
            if (key.contains("classloader.defineclass") || (owner.toLowerCase(Locale.ROOT).contains("classloader") && methodName.toLowerCase(Locale.ROOT).contains("defineclass"))) {
                setHigh("Detected classloader defineClass usage: " + owner + "." + methodName);
                return;
            }

            if (key.contains("java/io/fileoutputstream")
                    || key.contains("java/net/socket")
                    || key.contains("java/net/url")) {
                setMedium("Detected file or network API usage: " + owner + "." + methodName);
                return;
            }

            if (opcode == Opcodes.INVOKESTATIC && key.contains("loadlibrary")) {
                setHigh("Detected native library loading call");
            }

            // System.load is also native loading
            if (opcode == Opcodes.INVOKESTATIC && key.contains("java/lang/system.load")) {
                setHigh("Detected native library loading call: System.load");
                return;
            }

            // Unsafe usages
            if (owner.toLowerCase(Locale.ROOT).contains("sun/misc/unsafe") || owner.toLowerCase(Locale.ROOT).contains("jdk/internal/misc/unsafe") ) {
                setHigh("Detected use of Unsafe API: " + owner + "." + methodName);
                return;
            }

            // MethodHandles / invokedynamic related APIs
            if (owner.toLowerCase(Locale.ROOT).contains("java/lang/invoke/methodhandles") || key.contains("methodhandles.lookup")) {
                setMedium("Detected MethodHandles/ invokedynamic usage: " + owner + "." + methodName);
                return;
            }
        }

        private void inspectType(String type) {
            String lowerType = type.toLowerCase(Locale.ROOT);
            if (lowerType.contains("processbuilder") || lowerType.contains("classloader")) {
                setHigh("Detected dynamic process/class loading type allocation: " + type);
            } else if (lowerType.contains("socket") || lowerType.contains("fileoutputstream")) {
                setMedium("Detected medium-risk type allocation: " + type);
            }
        }

        private void inspectFieldInsn(int opcode, String owner, String name) {
            String key = (owner + "." + name).toLowerCase(Locale.ROOT);
            // Detect reflective accessibility toggles via AccessibleObject.setAccessible
            if (key.contains("reflect/accessibleobject.setaccessible") || key.contains("reflect/accessibleobject.isaccessible") || key.contains("accessibleobject.setaccessible")) {
                setMedium("Detected change to reflective accessibility: " + owner + "." + name);
            }
        }

        private void inspectLdc(String value) {
            String v = value.toLowerCase(Locale.ROOT);
            if (v.contains("java.lang.runtime") || v.contains("runtime.getruntime") || v.contains("java.lang.reflect") || v.contains("system.load")) {
                setMedium("Detected suspicious constant string referencing runtime/reflect/native: '" + value + "'");
            }
        }

        private void setHigh(String reason) {
            this.riskLevel = RiskLevel.HIGH;
            this.reason = reason;
        }

        private void setMedium(String reason) {
            if (this.riskLevel != RiskLevel.HIGH) {
                this.riskLevel = RiskLevel.MEDIUM;
                this.reason = reason;
            }
        }

        private MethodRisk toRisk() {
            String status = switch (riskLevel) {
                case HIGH -> "Dangerous";
                case MEDIUM -> "Suspicious";
                case LOW -> "Safe";
            };

            return MethodRisk.builder()
                    .methodName(methodName)
                    .status(status)
                    .riskLevel(riskLevel)
                    .riskReason(reason)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class AnalysisSummary {
        private String className;
        private List<MethodRisk> methodRisks;
        private List<ViolationRisk> violations;
        private Integer totalMethods;
        private Integer dangerousCount;
        private Integer safeCount;
        private RiskLevel riskLevel;
    }

    @Getter
    @Builder
    public static class MethodRisk {
        private String methodName;
        private String status;
        private RiskLevel riskLevel;
        private String riskReason;
    }

    @Getter
    @Builder
    public static class ViolationRisk {
        private String methodName;
        private RiskLevel riskLevel;
        private String reason;
    }
}
