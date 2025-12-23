package org.aopbuddy.plugin.infra.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * 右下角提醒弹窗工具类
 */
public class BalloonTipUtil {

  private final static String msgPre = "AopBuddy: ";


  public static void notifyError(@Nullable Project project,
      String content) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AopBuddy.NotificationGroup")
        .createNotification(msgPre + content, NotificationType.ERROR)
        .notify(project);
  }

  public static void notifyInfo(@Nullable Project project,
      String content) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AopBuddy.NotificationGroup")
        .createNotification(msgPre + content, NotificationType.INFORMATION)
        .notify(project);
  }
}