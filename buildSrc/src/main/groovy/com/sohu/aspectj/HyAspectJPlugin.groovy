package com.sohu.aspectj

import com.android.build.gradle.AppExtension
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

//gradleApi()中的Plugin
class HyAspectJPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        registerExtention(project)
        registerTransform(project)
//        troditionalAspectjProcess(project)
    }

    /**
     * 传统aspectj插件方式
     * @param project
     * @return
     */
    private troditionalAspectjProcess(Project project) {
        //gradle:3.2.1 中的AppExtension
        project.extensions.getByType(AppExtension).getApplicationVariants().all { variants ->
            JavaCompile javaCompiler = variants.javaCompiler
//            /Users/allenzhang/AspectJ/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
            println("----------------inpath-------------" + javaCompiler.destinationDir.toString())
//            /Users/allenzhang/.gradle/caches/transforms-1/files-1.1/design-28.0.0.aar/cd2e80a662cae66082356ce68f010bb8/jars/classes.jar
//          :/Users/allenzhang/.gradle/caches/transforms-1/files-1.1/appcompat-v7-28.0.0.aar/c54d7f8ccd6a59577eea394c9c81344c/jars/classes.jar
            println("----------------aspectpath-------------" + javaCompiler.classpath.asPath)
//            /Users/allenzhang/Library/Android/sdk/platforms/android-28/android.jar
//           :/Users/allenzhang/Library/Android/sdk/build-tools/28.0.3/core-lambda-stubs.jar
            println("----------------bootclasspath-------------" + javaCompiler.options.bootstrapClasspath.asPath)
//           -1.7 设置规范1.7，匹配java1.7
//           -showWeaveInfo，输出编织过程信息
//           -inpath class文件目录或者jar包， 源字节码，需要处理的类
//           -aspectpath  需要编织处理的class文件目录或者jar包
//            -d 存放编辑产生的class文件
//            -classpath ，所有class文件，源class，java包，编织时需要用到的一些处理类

            javaCompile.doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.7",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompiler.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompiler.classpath.asPath,
                        "-bootclasspath", javaCompiler.options.bootstrapClasspath.asPath
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
        }
    }

    private void registerTransform(Project project) {
        AppExtension appExtension = project.extensions.getByType(AppExtension)
        appExtension.registerTransform(new HyAjTransform(project))
    }

    private HyAjExtension registerExtention(Project project) {
        project.extensions.create(HyAjExtension.EXTENTION_NAME, HyAjExtension)
    }
}
