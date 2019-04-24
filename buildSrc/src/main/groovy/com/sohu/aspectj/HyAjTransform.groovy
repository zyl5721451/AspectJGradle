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
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.google.common.collect.ImmutableSet
import com.google.common.io.ByteStreams
import org.apache.commons.io.FileUtils
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.impldep.com.esotericsoftware.kryo.util.ObjectMap

import java.util.jar.JarEntry
import java.util.jar.JarFile

public class HyAjTransform extends Transform {
    static final TRANSFORM_NAME = "HyAjTransform"
    static final AJ_DIRECTORY_INCLUDE = "aj_directory_include"
    static final TRANS_DIRECTORY_EXCLUDE = "tr_directory_exclude"
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
//        System.setProperty("aspectj.multithreaded", "true")

        testReuslt(transformInvocation)
        HyAjExtension ajExtension = project.extensions.getByType(HyAjExtension)
        HyAjConstant ajConstant = new HyAjConstant(project,transformInvocation.context.variantName)
        if(!ajExtension.enable){
            super.transform(transformInvocation)
            return
        }
        if(transformInvocation.incremental) {
            boolean hasAspect = updateAspectClass(transformInvocation,ajConstant)
            if(!hasAspect) {
                super.transform(transformInvocation)
                return
            }
            println("---------hasAspectClass---incremental------")
            updateInputFiles(transformInvocation,ajConstant,ajExtension)

            if(ajConstant.aspectjClassChange||ajConstant.isIncludeFileChanged) {
                File directoryDes = transformInvocation.outputProvider.getContentLocation(AJ_DIRECTORY_INCLUDE, ajConstant.directoryIncludeContentTypes, ajConstant.directoryIncludeScopes, Format.JAR)
                FileUtils.deleteQuietly(directoryDes)
                ArrayList<String> inPath = new ArrayList<>()
                inPath.add(ajConstant.includeFilePath)
                aspectjProcess(project, ajConstant.aspectPathList, inPath, ajConstant.classPathList, directoryDes.absolutePath)
            }

            doUpdateAspect(transformInvocation, ajConstant,ajExtension)

        }else {
            transformInvocation.outputProvider.deleteAll()
            boolean hasAspect = cacheAspectClass(transformInvocation,ajConstant)
            if(!hasAspect) {
                super.transform(transformInvocation)
                return
            }
            println("---------hasAspectClass----full-----")
            copyExcludeClass(transformInvocation,ajConstant,ajExtension)

            File directoryDes = transformInvocation.outputProvider.getContentLocation(AJ_DIRECTORY_INCLUDE, ajConstant.directoryIncludeContentTypes, ajConstant.directoryIncludeScopes, Format.JAR)

            if (!directoryDes.parentFile.exists()) {
                FileUtils.forceMkdir(directoryDes.getParentFile())
            }

             FileUtils.deleteQuietly(directoryDes)


            ArrayList<String> inPath = new ArrayList<>()
            inPath.add(ajConstant.includeFilePath)

            aspectjProcess(project, ajConstant.aspectPathList, inPath, ajConstant.classPathList, directoryDes.absolutePath)

//            println("-------doWorkContinuously---inPath-----"+inPath.toString())
//            println("-------doWorkContinuously---outputJar-----"+directoryDes.absolutePath)
//            println("-------doWorkContinuously---aspectPath-----"+ajConstant.aspectPathList.toString())
//            println("-------doWorkContinuously---classPath----"+ajConstant.classPathList.toString())

            doaspect(transformInvocation, ajConstant,ajExtension)


        }
    }

    private void doUpdateAspect(TransformInvocation transformInvocation,HyAjConstant ajConstant,HyAjExtension ajExtension) {
       transformInvocation.inputs.each {
           it.jarInputs.each {JarInput jarInput ->
               if(jarInput.status != Status.NOTCHANGED) {

                   if(jarInput.status == Status.CHANGED||jarInput.status == Status.ADDED) {

                       JarFile jarFile = new JarFile(jarInput.file)
                       Enumeration<JarEntry> entries = jarFile.entries()
                       boolean include = true
                       while (entries.hasMoreElements()) {
                           JarEntry jarEntry = entries.nextElement()
                           String entryName = jarEntry.name
                           String transEntryName = entryName.replace(File.separator, ".")
                           boolean exclude = excludeFile(ajExtension, transEntryName)
                           if (exclude) {
                               include = false
                               break
                           }
                       }
                       if (include) {
                           File includeJar = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                           if(ajConstant.aspectjClassChange) {
                               ArrayList<File> inPath2 = new ArrayList<>()
                               inPath2.add(jarInput.file)
                               FileUtils.deleteQuietly(includeJar)
                               if (!includeJar.getParentFile()?.exists()) {
                                   includeJar.getParentFile()?.mkdirs()
                               }

                               aspectjProcess(project, ajConstant.aspectPathList, inPath2, ajConstant.classPathList, includeJar.absolutePath)
                           }else {
                               if(!includeJar.exists()) {
                                   ArrayList<File> inPath2 = new ArrayList<>()
                                   inPath2.add(jarInput.file)
                                   if (!includeJar.getParentFile()?.exists()) {
                                       includeJar.getParentFile()?.mkdirs()
                                   }
                                   aspectjProcess(project, ajConstant.aspectPathList, inPath2, ajConstant.classPathList, includeJar.absolutePath)
                               }
                           }

                       }


                   }



               }
           }
       }

    }

    private void doaspect(TransformInvocation transformInvocation,HyAjConstant ajConstant,HyAjExtension ajExtension) {
        transformInvocation.inputs.each {

            it.jarInputs.each { jarInput ->
                JarFile jarFile = new JarFile(jarInput.file)
                Enumeration<JarEntry> entries = jarFile.entries()
                boolean include = true
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement()
                    String entryName = jarEntry.name
                    String transEntryName = entryName.replace(File.separator, ".")
                    boolean exclude = excludeFile(ajExtension, transEntryName)
                    if (exclude) {
                        include = false
                        break
                    }
                }
                if (include) {
                    ArrayList<File> inPath2 = new ArrayList<>()
                    inPath2.add(jarInput.file)
                    File includeJar = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                    if (!includeJar.getParentFile()?.exists()) {
                        includeJar.getParentFile()?.mkdirs()
                    }

                    aspectjProcess(project, ajConstant.aspectPathList, inPath2, ajConstant.classPathList, includeJar.absolutePath)
                }
            }
        }
    }

    private void updateInputFiles(TransformInvocation transformInvocation,HyAjConstant ajConstant,HyAjExtension ajExtension) {
        transformInvocation.inputs.each {
            it.directoryInputs.each { DirectoryInput dirInput ->
                dirInput.changedFiles.each {File file,Status status ->
                    String path = file.absolutePath
                    String subPath = path.substring(dirInput.file.absolutePath.length())
                    String transPath = subPath.replace(File.separator,".")
                    boolean include = !excludeFile(ajExtension,transPath)
                    if(!ajConstant.isIncludeFileChanged&&include) {
                        ajConstant.isIncludeFileChanged = include
                    }
                    if(!ajConstant.isExcludeFileChanged&&!include) {
                        ajConstant.isExcludeFileChanged = !include
                    }
                    File target = new File((include?ajConstant.includeFilePath:ajConstant.excludeFilePath)+subPath)
                    switch (status) {
                        case Status.CHANGED:
                            FileUtils.deleteQuietly(target)
                            FileUtils.copyFile(file,target)
                            break
                        case Status.ADDED:
                            FileUtils.copyFile(file,target)
                            break
                        case Status.REMOVED:
                            FileUtils.deleteQuietly(target)
                            break
                    }

                    if(ajConstant.isIncludeFileChanged) {
                        File directoryDes = transformInvocation.outputProvider.getContentLocation(AJ_DIRECTORY_INCLUDE, ajConstant.directoryIncludeContentTypes, ajConstant.directoryIncludeScopes, Format.JAR)
                        FileUtils.deleteQuietly(directoryDes)
                    }

                    if(ajConstant.isExcludeFileChanged) {
                        File excludeJar = transformInvocation.getOutputProvider().getContentLocation(TRANS_DIRECTORY_EXCLUDE, ajConstant.directoryIncludeContentTypes,
                                ajConstant.directoryIncludeScopes, Format.JAR)
                        FileUtils.deleteQuietly(excludeJar)
                        mergeJar(new File(ajConstant.excludeFilePath), excludeJar)
                    }
                }
            }

            it.jarInputs.each {JarInput jarInput ->
                if(jarInput.status != Status.NOTCHANGED) {
                    String filePath = jarInput.file.absolutePath
                    File outputJar = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    if(jarInput.status == Status.CHANGED) {
                        FileUtils.deleteQuietly(outputJar)
                        ajConstant.classPathList.add(jarInput.file.absolutePath)
                    }else if(jarInput.status == Status.ADDED) {
                        ajConstant.classPathList.add(jarInput.file.absolutePath)

                    }else if(jarInput.status == Status.REMOVED) {
                        FileUtils.deleteQuietly(outputJar)
                        ajConstant.classPathList.remove(jarInput.file.absolutePath)
                    }

                    JarFile jarFile = new JarFile(jarInput.file)
                    Enumeration<JarEntry> entries = jarFile.entries()
                    boolean include = true
                    while (entries.hasMoreElements()){
                        JarEntry jarEntry = entries.nextElement()
                        String entryName = jarEntry.name
                        String transEntryName = entryName.replace(File.separator,".")
                        boolean exclude = excludeFile(ajExtension,transEntryName)
                        if(exclude) {
                            include = false
                            break
                        }
                    }

                    if(!include) {
                        FileUtils.copyFile(jarInput.file,outputJar)
                    }


                }
            }
        }


    }

    private void copyExcludeClass(TransformInvocation transformInvocation,HyAjConstant ajConstant,HyAjExtension ajExtension) {
        transformInvocation.inputs.each {
            it.directoryInputs.each {dirInput ->
                dirInput.file.eachFileRecurse { file ->
                    if(HyAjUtil.isClassFile(file)){
                        String path = file.absolutePath
                        String subPath = path.substring(dirInput.file.absolutePath.length())
                        String transPath = subPath.replace(File.separator,".")
                        boolean include = !excludeFile(ajExtension,transPath)


                        if(include) {
                            File includeDirectory = new File(ajConstant.includeFilePath+subPath)
                            if(!includeDirectory.getParentFile()?.exists()) {
                                includeDirectory.getParentFile()?.mkdirs()
                            }
                            FileUtils.copyFile(file,includeDirectory)
                        }else {
                            File exludeDirectory = new File(ajConstant.excludeFilePath+subPath)
                            if(!exludeDirectory.getParentFile()?.exists()) {
                                exludeDirectory.getParentFile()?.mkdirs()
                            }
                            FileUtils.copyFile(file,exludeDirectory)
                        }
                    }
                }

                //put exclude files into jar
                if (HyAjUtil.countOfFiles(new File(ajConstant.excludeFilePath)) > 0) {
                    File excludeJar = transformInvocation.getOutputProvider().getContentLocation(TRANS_DIRECTORY_EXCLUDE, ajConstant.directoryIncludeContentTypes,
                            ajConstant.directoryIncludeScopes, Format.JAR)
                    mergeJar(new File(ajConstant.excludeFilePath), excludeJar)
                }


//            if(HyAjUtil.countOfFiles(new File(ajConstant.excludeFilePath))>0) {
//                File directoryDes = transformInvocation.outputProvider.getContentLocation("excludess",dirInput.contentTypes,dirInput.scopes,Format.JAR)
//                mergeJar(new File(ajConstant.excludeFilePath),directoryDes)
//            }
            }



            it.jarInputs.each { jarInput->
                JarFile jarFile = new JarFile(jarInput.file)
                Enumeration<JarEntry> entries = jarFile.entries()
                boolean include = true
                while (entries.hasMoreElements()){
                    JarEntry jarEntry = entries.nextElement()
                    String entryName = jarEntry.name
                    String transEntryName = entryName.replace(File.separator,".")
                    boolean exclude = excludeFile(ajExtension,transEntryName)
                    if(exclude) {
                        include = false
                        break
                    }
                }
                if(!include) {
                    File excludeJar = transformInvocation.outputProvider.getContentLocation(jarInput.name,jarInput.contentTypes,jarInput.scopes,Format.JAR)
                    FileUtils.copyFile(jarInput.file,excludeJar)
                }
                ajConstant.classPathList.add(jarInput.file.absolutePath)
            }
        }
    }

    private boolean updateAspectClass(TransformInvocation invocation,HyAjConstant ajConstant){
        invocation.inputs.each { it ->
            it.directoryInputs.each { dirInput ->
                dirInput.changedFiles.each { File file,Status status ->
                    if(HyAjUtil.isAspectClass(file)) {
                        ajConstant.aspectjClassChange = true
                        String path = file.absolutePath
                        String subPath = path.substring(dirInput.file.absolutePath.length())
                        File cacheFile = new File(ajConstant.aspectPath + subPath)
                        switch (status) {
                            case Status.REMOVED:
                                println("-----updateAspectClass----REMOVED---"+cacheFile)
                                FileUtils.deleteQuietly(cacheFile)
                                break
                            case Status.ADDED:
                                println("-----updateAspectClass----ADDED---"+cacheFile)
                                FileUtils.copyFile(file,cacheFile)
                                break
                            case Status.CHANGED:
                                println("-----updateAspectClass----CHANGED---"+cacheFile)
                                FileUtils.deleteQuietly(cacheFile)
                                FileUtils.copyFile(file,cacheFile)
                                break
                        }
                    }

                }

            }

            it.jarInputs.each {JarInput jarInput ->
                if(jarInput.status != Status.NOTCHANGED) {
                    JarFile jarFile = new JarFile(jarInput.file)
                    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries()
                    while (jarEntryEnumeration.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnumeration.nextElement()
                        String entryName = jarEntry.name
                        if(!jarEntry.isDirectory()&&HyAjUtil.isClassFile(entryName)) {
                            byte[] bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry))
                            File cacheFile = new File(ajConstant.aspectPath + File.separator + entryName)
                            if(HyAjUtil.isAspectClass(bytes)) {
                                ajConstant.aspectjClassChange = true
                                if(jarInput.status == Status.REMOVED) {
                                    FileUtils.deleteQuietly(cacheFile)
                                }else if(jarInput.status == Status.ADDED) {
                                    FileUtils.copyFile(jarInput.file,cacheFile)
                                }else if(jarInput.status == Status.CHANGED) {
                                    FileUtils.deleteQuietly(cacheFile)
                                    FileUtils.copyFile(jarInput.file,cacheFile)
                                }
                            }
                        }
                    }
                    jarFile.close()
                }

            }
        }
        return HyAjUtil.countOfFiles(ajConstant.aspectFileDir)>0
    }

    private boolean cacheAspectClass(TransformInvocation invocation,HyAjConstant ajConstant) {
        invocation.inputs.each {
            it.jarInputs.each { jarInput ->
                JarFile jarFile = new JarFile(jarInput.file)
                Enumeration<JarEntry> entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement()
                    String entryName = jarEntry.name
                    byte[] bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry))
                    if (!jarEntry.isDirectory() && HyAjUtil.isClassFile(entryName) && HyAjUtil.isAspectClass(bytes)) {
                        File cacheFile = new File(ajConstant.aspectPath + File.separator + entryName)
                        if(!cacheFile.parentFile.exists()) {
                            cacheFile.parentFile.mkdirs()
                        }
                        FileUtils.copyFile(jarInput.file, cacheFile)
                    }
                }
                jarFile.close()
            }

            it.directoryInputs.each { dirInput ->
                dirInput.file.eachFileRecurse { file ->
                    if (HyAjUtil.isClassFile(file)) {
                        String path = file.absolutePath
                        String subPath = path.substring(dirInput.file.absolutePath.length())
                        if (HyAjUtil.isAspectClass(file)) {
                            File cacheFile = new File(ajConstant.aspectPath + subPath)
                            if(!cacheFile.parentFile.exists()) {
                                cacheFile.parentFile.mkdirs()
                            }
                            FileUtils.copyFile(file, cacheFile)
                        }
                    }
                }
            }
        }


        return HyAjUtil.countOfFiles(ajConstant.aspectFileDir)>0
    }

    public String getStrJoint(ArrayList<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i=0;i<strings.size();i++) {
            builder.append(strings.get(i))
            if(i<strings.size()-1) {
               builder.append(":")
            }

        }
        return builder.toString()

    }


    private aspectjProcess(Project project,ArrayList<String> aspectFilePath,ArrayList<String> inFilePath,ArrayList<String> classFilePath,String outDir) {



        println("------aspectjProcess-------"+getStrJoint(inFilePath))
        println("------aspectjProcess-------"+getStrJoint(classFilePath))
        println("------aspectjProcess-------"+getStrJoint(aspectFilePath))
        println("------aspectjProcess-------"+project.android.bootClasspath.join(File.pathSeparator))

        String[] args = [
                "-showWeaveInfo",
                "-encoding", "UTF-8",
                "-source", "1.7",
                "-target", "1.7",
                "-inpath", getStrJoint(inFilePath),
                "-aspectpath", getStrJoint(classFilePath),
                "-outjar", outDir,
                "-classpath", getStrJoint(classFilePath),
                "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
        ]
        MessageHandler handler = new MessageHandler(true)
        new Main().run(args, handler)
        def log = project.logger
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    println("----------------ERROR-------------" + message.message)
                    log.error message.message, message.thrown
                    break
                case IMessage.WARNING:
                case IMessage.INFO:
                    println("----------------WARNING-------------" + message.message)
                    log.info message.message, message.thrown
                    break
                case IMessage.DEBUG:
                    println("----------------DEBUG-------------" + message.message)
                    log.debug message.message, message.thrown
                    break
            }
        }
    }


    private boolean excludeFile(HyAjExtension ajExtension,String transPath) {
        for(String filter:ajExtension.excludes) {
            if(transPath.contains(filter)) {
                return true
            }
        }
        return false
    }

    private boolean includeFile(HyAjExtension ajExtension,String transPath) {
        for(String filter:ajExtension.includes) {
            if(transPath.contains(filter)) {
                return true
            }
        }
        return false
    }

    static void mergeJar(File sourceDir, File targetJar) {
        if (sourceDir == null) {
            throw new IllegalArgumentException("sourceDir should not be null")
        }

        if (targetJar == null) {
            throw new IllegalArgumentException("targetJar should not be null")
        }

        if (!targetJar.parentFile.exists()) {
            FileUtils.forceMkdir(targetJar.getParentFile())
        }

        FileUtils.deleteQuietly(targetJar)

        JarMerger jarMerger = new JarMerger(targetJar)
        try {
            jarMerger.setFilter(new JarMerger.IZipEntryFilter() {
                @Override
                boolean checkEntry(String archivePath) throws JarMerger.IZipEntryFilter.ZipAbortException {
                    return archivePath.endsWith(SdkConstants.DOT_CLASS)
                }
            })

            jarMerger.addFolder(sourceDir)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            jarMerger.close()
        }
    }

    /**
     * 测试输出结果
     * @param transformInvocation
     */
    private void testReuslt(TransformInvocation transformInvocation) {
        for (TransformInput input : transformInvocation.inputs) {
            input.jarInputs.each {
                File file = it.getFile()
                JarFile jarFile = new JarFile(file)
//                 ---transformjarInputs----android.arch.lifecycle:viewmodel:1.1.1
//                 ---transformjarInputs----com.android.support:interpolator:28.0.0
//                println("---transformjarInputs----" + it.name)
            }

            input.directoryInputs.each {
//                 ---transformdirectoryInputs----583a71e21b19b403c1b00d8824158929b6045f18
//                println("---transformdirectoryInputs----" + it.name)

                //name - a unique name for the content. For a given set of scopes/types/format it must be unique.
//                 ---transformdirectoryInputs----/Users/allenzhang/AspectJ/app/build/intermediates/transforms/HyAjTransform/release/0
                def dest = transformInvocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
//                println("---transformdirectoryInputs----" + dest.path)
            }
        }
    }
}
