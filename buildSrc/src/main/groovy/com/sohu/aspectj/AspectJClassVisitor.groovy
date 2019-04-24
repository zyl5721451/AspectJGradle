package com.sohu.aspectj

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
/**
 * Created by allenzhang on 2019/2/28.
 */
public class AspectJClassVisitor extends ClassVisitor{
    boolean isAspectJClass = false

    AspectJClassVisitor(ClassWriter classVisitor) {
        super(Opcodes.ASM5,classVisitor)
    }


    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        isAspectJClass |= (desc == "Lorg/aspectj/lang/annotation/Aspect;")
        return super.visitAnnotation(desc, visible)
    }
}
