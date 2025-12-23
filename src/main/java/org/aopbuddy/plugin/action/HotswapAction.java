package org.aopbuddy.plugin.action;

import cn.hutool.core.io.FileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.aopbuddy.plugin.infra.util.BalloonTipUtil;
import org.aopbuddy.plugin.infra.util.DebugToolsIdeaClassUtil;
import org.aopbuddy.plugin.service.JvmService;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

public class HotswapAction extends AnAction {

  private static Logger log = Logger.getInstance(HotswapAction.class);

  private Project project;

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    this.project = e.getProject();
    List<VirtualFile> virtualFiles = getDiff(e);
    localCompiler(virtualFiles);
  }

  private List<VirtualFile> getDiff(AnActionEvent anActionEvent) {
    Object workflowHandler = anActionEvent.getDataContext()
        .getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
    if (workflowHandler == null) {
      return new ArrayList<>();
    }
    List<VirtualFile> virtualFiles = new ArrayList<>();
    // 从 workflowHandler 或 action 中获取
    if (workflowHandler instanceof AbstractCommitWorkflowHandler) {
      List<Change> changes = ((AbstractCommitWorkflowHandler<?, ?>) workflowHandler).getUi()
          .getIncludedChanges();
      for (Change change : changes) {
        FilePath path = ChangesUtil.getFilePath(change);
        VirtualFile vf = path.getVirtualFile();
        virtualFiles.add(vf);
      }
      List<FilePath> unversioned = ((AbstractCommitWorkflowHandler<?, ?>) workflowHandler).getUi()
          .getIncludedUnversionedFiles();
      for (FilePath path : unversioned) {
        VirtualFile vf = path.getVirtualFile();
        virtualFiles.add(vf);
      }
    }
    return virtualFiles;
  }

  private void localCompiler(List<VirtualFile> virtualFiles) {
    // 获取编译管理器实例
    CompilerManager compilerManager = CompilerManager.getInstance(project);
    // 创建编译范围
    CompileScope scope = compilerManager.createFilesCompileScope(
        virtualFiles.toArray(new VirtualFile[0]));
    // 编译这些文件
    compilerManager.compile(scope, (aborted, errors, warnings, compileContext) -> {
      if (errors > 0) {
        // 记录错误日志
        log.error("Compilation failed with errors: " + errors + ", warnings: " + warnings);
        return;
      }
      // 编译成功后，更新文件并上传
      ReadAction.nonBlocking(() -> {
            Set<VirtualFile> allOutputs = new HashSet<>();
            List<ClassFilePath> allOutputClasses = new ArrayList<>();
            // 遍历所有虚拟文件
            virtualFiles.forEach((selectedFile) -> {
              // 获取输出目录
              VirtualFile outputDirectory = compileContext.getModuleOutputDirectory(
                  compileContext.getModuleByFile(selectedFile));
              if (outputDirectory == null) {
                return;
              }
              // 如果当前输出目录已经收集过，跳过此目录
              if (allOutputs.contains(outputDirectory)) {
                return;
              }
              allOutputs.add(outputDirectory);
              // 获取源文件路径和内容
              String sourceFilePath = selectedFile.getPath();
              Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
              String packageName = DebugToolsIdeaClassUtil.getPackageName(document.getText());
              if (packageName == null) {
                return;
              }
              String className = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1)
                  .replace(".java", "");
              // 构建源文件的基本名称
              String sourceFileBaseName = packageName.replace(".", "/") + "/" + className;
              // 收集编译后的类文件
              List<ClassFilePath> outputClasses = collectClassFiles(outputDirectory,
                  sourceFileBaseName);
              // 如果未找到类文件，再次刷新输出目录并尝试收集
              if (outputClasses.isEmpty()) {
                outputDirectory.refresh(false, true);
                outputClasses = collectClassFiles(outputDirectory, sourceFileBaseName);
              }
              allOutputClasses.addAll(outputClasses);
            });
            if (CollectionUtils.isEmpty(allOutputClasses)) {
              return null;
            }
            String s = project.getService(JvmService.class).hotSwap(allOutputClasses);
            if ("success".equals(s)) {
              BalloonTipUtil.notifyInfo(project, "热部署成功");
            } else {
              BalloonTipUtil.notifyError(project, s);
            }
            return null;
          })
          .submit(AppExecutorUtil.getAppExecutorService())
          .onError(throwable -> {

          });
    });
  }

  private static List<ClassFilePath> collectClassFiles(VirtualFile directory,
      final String sourceFileBaseName) {
    // 创建一个空的列表 outPutClasses，用于存储匹配的 ClassFilePath 对象
    final List<ClassFilePath> outPutClasses = new ArrayList<>();

    // 使用 VfsUtilCore 递归访问目录中的所有文件
    VfsUtilCore.visitChildrenRecursively(directory, new VirtualFileVisitor<>() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        // 检查文件是否为 .class 文件，并且路径与 sourceFileBaseName 匹配
        if (file.getPath().endsWith(".class")
            && isMatchPathClass(file.getPath(), sourceFileBaseName)) {
          // 创建新的 ClassFilePath 对象并设置路径和类名
          ClassFilePath classFilePath = new ClassFilePath();
          classFilePath.setFullPath(file.getPath());
          byte[] bytes = FileUtil.readBytes(classFilePath.getFullPath());
          classFilePath.setPayload(Base64.getEncoder().encodeToString(bytes));
          String className = file.getPath()
              .substring(file.getPath().lastIndexOf(sourceFileBaseName),
                  file.getPath().length() - ".class".length())
              .replace("/", ".");
          classFilePath.setClassName(className);
          // 将 ClassFilePath 对象添加到列表中
          outPutClasses.add(classFilePath);
        }
        // 返回 true 继续递归访问其他文件
        return true;
      }
    });

    // 返回收集到的 ClassFilePath 对象列表
    return outPutClasses;
  }

  public static boolean isMatchPathClass(String fullPath, String classnamePath) {
    Pattern pattern = Pattern.compile(classnamePath + "(\\$\\w+)?\\.class");
    Matcher matcher = pattern.matcher(fullPath);
    return matcher.find();
  }

  @Data
  public static class ClassFilePath {

    private String className;
    private String fullPath;
    private String payload;
  }
}
