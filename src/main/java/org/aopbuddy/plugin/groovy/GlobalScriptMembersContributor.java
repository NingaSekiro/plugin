package org.aopbuddy.plugin.groovy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.ElementClassHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;

public class GlobalScriptMembersContributor extends NonCodeMembersContributor {

  @Override
  public void processDynamicElements(@NotNull PsiType qualifierType,
      @NotNull PsiScopeProcessor processor,
      @NotNull PsiElement place,
      @NotNull ResolveState state) {

    PsiElement psiFile = place.getContainingFile();
    if (!(psiFile instanceof GroovyFile groovyFile)) {
      return;
    }
    if (!groovyFile.isScript()) {
      return;
    }

    ElementClassHint hint = processor.getHint(ElementClassHint.KEY);
    if (hint != null && !hint.shouldProcess(ElementClassHint.DeclarationKind.METHOD)) {
      return;
    }

    PsiManager manager = place.getManager();

    // getObject(Class)
    LightMethodBuilder getObject = new LightMethodBuilder(manager, GroovyLanguage.INSTANCE,
        "getObject")
        .setMethodReturnType(PsiType.getJavaLangObject(manager, place.getResolveScope()))
        .addParameter("clazz", "java.lang.Class");
    if (!processor.execute(getObject, state)) {
      return;
    }

    // toJson(Object)
    LightMethodBuilder toJson = new LightMethodBuilder(manager, GroovyLanguage.INSTANCE, "toJson")
        .setMethodReturnType(PsiType.getJavaLangString(manager, place.getResolveScope()))
        .addParameter("value", "java.lang.Object");
    if (!processor.execute(toJson, state)) {
      return;
    }

    // jadClass(String)
    LightMethodBuilder jadClass = new LightMethodBuilder(manager, GroovyLanguage.INSTANCE,
        "jadClass")
        .setMethodReturnType(PsiType.getJavaLangString(manager, place.getResolveScope()))
        .addParameter("className", "java.lang.String");
    processor.execute(jadClass, state);
  }
}
