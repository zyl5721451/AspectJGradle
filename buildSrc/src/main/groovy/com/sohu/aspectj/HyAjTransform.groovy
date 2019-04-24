package com.sohu.aspectj

import com.android.SdkConstants
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.io.ByteStreams
import org.apache.commons.io.FileUtils
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class HyAjTransform extends Transform {
    static final TRANSFORM_NAME = "HyAjTransform"
    Project project

    HyAjTransform(Project project) {
        this.project = project
    }

    @Override
    public String getName() {
        return TRANSFORM_NAME
    }


    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT

    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        setMultiThreadEnable()
        HyAjExtension ajExtension = project.extensions.getByType(HyAjExtension)
        HyAjConstant ajConstant = new HyAjConstant(project,transformInvocation.context.variantName)
        if(!ajExtension.enable){
            super.transform(transformInvocation)
            return
        }
        HyAspectJProcess aspectJProcess = new HyAspectJProcess(transformInvocation,project,ajConstant,ajExtension)
        aspectJProcess.process()
    }

    private String setMultiThreadEnable() {
        System.setProperty("aspectj.multithreaded", "true")
    }
}
