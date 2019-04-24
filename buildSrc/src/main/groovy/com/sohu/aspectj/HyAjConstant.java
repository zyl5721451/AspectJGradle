package com.sohu.aspectj;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInput;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.builder.model.AndroidProject;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by allenzhang on 2019/4/16.
 */
public class HyAjConstant {
    private String variantName;
    Project project;
    String includeFilePath;
    String excludeFilePath;
    String cachePath;
    public String aspectPath;
    public boolean aspectjClassChange = false;
    public boolean isIncludeFileChanged = false;
    public boolean isExcludeFileChanged = false;
    public ArrayList<String> classPathList = new ArrayList<>();
    public ArrayList<String> aspectPathList = new ArrayList<>();

    Set<QualifiedContent.ContentType> directoryIncludeContentTypes = TransformManager.CONTENT_CLASS;
    Set<QualifiedContent.Scope> directoryIncludeScopes = Sets.immutableEnumSet(QualifiedContent.Scope.EXTERNAL_LIBRARIES);

    public HyAjConstant(Project project,String variantName) {
        this.project = project;
        this.variantName = variantName;
        init();
    }

    public void init(){
        cachePath = project.getBuildDir().getAbsolutePath() + File.separator + AndroidProject.FD_INTERMEDIATES + "/hyaj/" + variantName;
        aspectPath = cachePath + File.separator + "aspecs";
        includeFilePath = cachePath + File.separator + "includefiles";
        excludeFilePath = cachePath + File.separator + "excludefiles";
        if(!getAspectFileDir().exists()) {
            getAspectFileDir().mkdirs();
        }
        if(!getExcludeFileDir().exists()) {
            getExcludeFileDir().mkdirs();
        }
        if(!getIncludeFileDir().exists()) {
            getIncludeFileDir().mkdirs();
        }
        classPathList.add(includeFilePath);
        classPathList.add(excludeFilePath);
        aspectPathList.add(aspectPath);


    }

    public File getAspectFileDir() {
        return new File(aspectPath);
    }

    public File getIncludeFileDir() {
        return new File(includeFilePath);
    }

    public File getExcludeFileDir() {
        return new File(excludeFilePath);
    }
}
