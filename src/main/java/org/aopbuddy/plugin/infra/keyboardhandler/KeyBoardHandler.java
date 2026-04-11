package org.aopbuddy.plugin.infra.keyboardhandler;

import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandlerAdapter;

public class KeyBoardHandler extends CefKeyboardHandlerAdapter {

  private static final int F_12_KEY = 123;
  private final JBCefBrowser jbCefBrowser;

  public KeyBoardHandler(JBCefBrowser jbCefBrowser) {
    this.jbCefBrowser = jbCefBrowser;
  }

  @Override
  public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
    if (event.windows_key_code == F_12_KEY) {
      jbCefBrowser.openDevtools();
      return true;
    }
    return false;
  }
}
