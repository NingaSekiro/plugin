/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.aopbuddy.plugin.infra.util;

import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class I18nUtil extends DynamicBundle {

  private static final String BUNDLE = "i18n.JavaInsight";

  private I18nUtil() {
    super(I18nUtil.BUNDLE);
  }

  public static String message(@NotNull @PropertyKey(resourceBundle = I18nUtil.BUNDLE) String key,
      Object... params) {
    ClassLoader classLoader = I18nUtil.class.getClassLoader();
    ResourceBundle resourceBundle = ResourceBundle.getBundle(I18nUtil.BUNDLE,
        DynamicBundle.getLocale(), classLoader);
    return AbstractBundle.message(resourceBundle, key, params);
  }
}