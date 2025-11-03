package org.aopbuddy.plugin.infra.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;
import java.util.stream.Collectors;

public class JavaPackageCollector {

    public static Set<String> collectAllJavaPackages(Project project) {
        return ReadAction.compute(() -> {
            Set<String> packageNames = new HashSet<>();

            // 搜索所有 Java 文件
            Collection<VirtualFile> javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
            PsiManager psiManager = PsiManager.getInstance(project);

            for (VirtualFile vf : javaFiles) {
                PsiFile psiFile = psiManager.findFile(vf);
                if (psiFile instanceof PsiJavaFile javaFile) {
                    String pkg = javaFile.getPackageName();
                    if (!pkg.isEmpty()) {
                        packageNames.add(pkg);
                    }
                }
            }

            return packageNames.stream()
                    .filter(pkg -> packageNames.stream()
                            .noneMatch(otherPkg -> !pkg.equals(otherPkg) && pkg.startsWith(otherPkg + "."))
                    )
                    .collect(Collectors.toSet());
        });
    }
}
