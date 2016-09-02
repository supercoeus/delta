package org.byteam.tp.patch.task

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.ProGuardTransform
import org.apache.commons.io.FileUtils
import org.byteam.tp.patch.TpPlugin
import org.byteam.tp.patch.bean.Patch
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
/**
 * Backup original files.
 *
 * @Author: chenenyu
 * @Created: 16/8/24 18:50.
 */
class TpBackupTask extends DefaultTask {

    @Input
    Patch mPatch

    @Input
    ApplicationVariant mVariant

    @TaskAction
    void backupPatch() {
        File originalDir = new File(mPatch.originalPath)
        originalDir.mkdirs()
        FileUtils.cleanDirectory(originalDir)

        saveMapping()
        handleDexTask()
    }

    /**
     * Save mapping.txt
     */
    private void saveMapping() {
        if (mVariant.buildType.minifyEnabled) {
            String transformClassesAndResourcesWithProguardForVariant = "transformClassesAndResourcesWithProguardFor${mVariant.name.capitalize()}"
            def proguardTask = project.tasks.findByName(transformClassesAndResourcesWithProguardForVariant) as TransformTask
            if (proguardTask) {
                def mappingFile = (proguardTask.transform as ProGuardTransform).mappingFile
                FileUtils.copyFile(mappingFile, mPatch.mappingFile)
            } else {
                println("Task:${transformClassesAndResourcesWithProguardForVariant} not found.")
            }
        }
    }

    /**
     * 处理DexTransform,保存dex。
     */
    private void handleDexTask() {
        String transformClassesWithDexForVariant = "transformClassesWithDexFor${mVariant.name.capitalize()}"
        def dexTask = project.tasks.findByName(transformClassesWithDexForVariant) as TransformTask
        if (dexTask) {
            copyAllDexToPatch(project, mPatch)
        } else {
            println("Task:${transformClassesWithDexForVariant} not found.")
        }
    }

    /**
     * 将dex拷贝到patch目录下。
     */
    private void copyAllDexToPatch(Project project, Patch patch) {
        File dexDir = TpPlugin.getDexFolder(project, patch)
        if (!dexDir.exists()) {
            throw new IllegalArgumentException(String.format("Can't find dex directory: %s", dexDir.absolutePath))
        }
        File originalDexDir = new File(patch.originalDexPath)
        originalDexDir.mkdirs()
        FileUtils.cleanDirectory(originalDexDir)
        dexDir.eachFile { dex ->
            FileUtils.copyFileToDirectory(dex, originalDexDir)
        }
    }

}