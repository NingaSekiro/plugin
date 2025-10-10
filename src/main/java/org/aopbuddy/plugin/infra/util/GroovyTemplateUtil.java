package org.aopbuddy.plugin.infra.util;

import com.intellij.openapi.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class GroovyTemplateUtil {

    public static String generateCode(String template, Map<String, String> params) {
        try {
            String resourcePath = "groovy/" + template + ".groovy";
            InputStream inputStream = GroovyTemplateUtil.class.getClassLoader().getResourceAsStream(resourcePath);
            String text = "";
            if (inputStream != null) {
                text = FileUtil.loadTextAndClose(inputStream);
            }
            for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
                // 确保生成的Groovy代码中的通配符参数被正确地作为字符串传递
                String quotedValue = "\"" + stringStringEntry.getValue() + "\"";
                text = text.replaceAll("params." + stringStringEntry.getKey(), quotedValue);
            }
            return text;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
