package org.aopbuddy.plugin.linemarker;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.aopbuddy.plugin.infra.util.IconUtil;
import org.aopbuddy.plugin.service.ConsoleStateService;
import org.aopbuddy.plugin.service.WebSocketClientService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WatchMethodLineMarkerProvider extends LineMarkerProviderDescriptor implements
    LineMarkerProvider {

  @Override
  public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof PsiIdentifier)) {
      return null;
    }
    PsiElement parent = element.getParent();
    if (!(parent instanceof PsiMethod method)) {
      return null;
    }
    if (method.getContainingClass() == null
        || method.getContainingClass().getQualifiedName() == null) {
      return null;
    }
    String methodKey = method.getContainingClass().getQualifiedName() + "#" + method.getName();
    Project project = element.getProject();
    ConsoleStateService state = project.getService(ConsoleStateService.class);
    if (!state.getWatchedMethodKeys().contains(methodKey)) {
      return null;
    }
    Icon icon = IconUtil.getPluginIcon();
    GutterIconNavigationHandler<PsiElement> nav = (e, elt) -> {
      WebSocketClientService ws = project.getService(WebSocketClientService.class);
      ws.sendUnwatchRequest(method.getContainingClass().getQualifiedName(), method.getName());
    };
    return new LineMarkerInfo<>(
        element,
        element.getTextRange(),
        icon,
        psiElement -> "点击取消 Watch",
        nav,
        Alignment.RIGHT,
        () -> "点击取消 Watch"
    );
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
      @NotNull Collection<? super LineMarkerInfo<?>> result) {
  }

  @Override
  public @NotNull String getName() {
    return "AopBuddy Watch Method";
  }
}
