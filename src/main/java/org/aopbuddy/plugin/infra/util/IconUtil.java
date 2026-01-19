package org.aopbuddy.plugin.infra.util;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

public class IconUtil {

  public static Icon getPluginIcon() {
    return IconLoader.getIcon("/icons/icon.svg", IconUtil.class);
  }
}