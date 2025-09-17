package org.aopbuddy.plugin.action;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.ui.breakpoints.JavaWildcardMethodBreakpointType;
import com.intellij.debugger.ui.breakpoints.WildcardMethodBreakpoint;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.*;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
// FIX: Ensure both BreakpointType and its Properties are imported from the same package.
import com.intellij.debugger.ui.breakpoints.JavaMethodBreakpointType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.notification.ActionCenter.showNotification;

public class MyRemoteDebugAction extends AnAction {

    private XBreakpoint methodEntryBreakpoint;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        actionPerformed2(e);
    }

    public void actionPerformed0(@NotNull AnActionEvent e) {

        // 获取当前 Debug 会话
        XDebugSession session = XDebuggerManager.getInstance(e.getProject()).getCurrentSession();
        if (session == null) {
            System.err.println("no session");
            return;
        }
        // 获取当前栈帧
        XStackFrame frame = session.getCurrentStackFrame();
        if (frame == null) {
            System.err.println("no frame");
            return;
        }

        // 获取 Evaluator
        XDebuggerEvaluator evaluator = frame.getEvaluator();
        if (evaluator == null) {
            System.err.println("no evaluator");
            return;
        }
        // 要执行的表达式 (⚠️ 在远程 JVM 上执行！)
        String expr = "java.nio.file.Files.write(" +
                "java.nio.file.Paths.get(\"D:/tmp/remote-output.jar\")," +
                "new byte[]{65,66,67,68})"; // 写入 ABCD
        // 构建 XExpression
        XExpression xExpression = XExpressionImpl.fromText(expr);
        // 异步执行
        evaluator.evaluate(xExpression, new XDebuggerEvaluator.XEvaluationCallback() {
            @Override
            public void evaluated(@NotNull com.intellij.xdebugger.frame.XValue result) {
                System.out.println("success: " + result.toString());
                // 执行完成后删除方法入口断点
                removeMethodEntryBreakpoint(session);
            }

            @Override
            public void errorOccurred(@NotNull String errorMessage) {
                System.err.println("fail: " + errorMessage);
                // 执行失败时也删除方法入口断点
                removeMethodEntryBreakpoint(session);
            }
        }, frame.getSourcePosition());
    }


    public void actionPerformed2(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 获取当前调试会话
        XDebugSession session = XDebuggerManager.getInstance(e.getProject()).getCurrentSession();


        // 在后台线程中添加断点
        addMethodBreakpoint(project);
    }

    private void addMethodBreakpoint(Project project) {
        registerDebugSessionListener(project);
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        XBreakpointManager breakpointManager = debuggerManager.getBreakpointManager();
        JavaWildcardMethodBreakpointType breakpointType = new JavaWildcardMethodBreakpointType();
        JavaMethodBreakpointProperties properties = new JavaMethodBreakpointProperties("Main", "test");
        properties.EMULATED = true; // 使用模拟模式提高性能
        properties.WATCH_ENTRY = true; // 监听方法进入
        properties.WATCH_EXIT = false; // 不监听方法退出
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteAction.run(() -> {
                XBreakpoint<JavaMethodBreakpointProperties> javaMethodBreakpointPropertiesXBreakpoint = breakpointManager.addBreakpoint(breakpointType, properties);
                javaMethodBreakpointPropertiesXBreakpoint.setSuspendPolicy(SuspendPolicy.THREAD);
            });
        });

    }


    /**
     * 删除方法入口断点
     */
    private void removeMethodEntryBreakpoint(XDebugSession session) {
        try {
            if (methodEntryBreakpoint != null) {
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(session.getProject()).getBreakpointManager();
                breakpointManager.removeBreakpoint(methodEntryBreakpoint);
                methodEntryBreakpoint = null;
                System.out.println("方法入口断点已删除");
            }
        } catch (Exception e) {
            System.err.println("删除方法入口断点失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerDebugSessionListener(@NotNull Project project) {
        XDebugSession currentSession = XDebuggerManager.getInstance(project).getCurrentSession();
        // 创建新的会话监听器
        XDebugSessionListener mySessionListener = new XDebugSessionListener() {
            public void sessionPaused(@NotNull XDebugSession session) {
                // 检查是否是我们的方法断点触发的暂停
                XStackFrame currentFrame = session.getCurrentStackFrame();
                if (currentFrame != null) {
                    // 获取当前断点
                    SuspendContextImpl context = (SuspendContextImpl) session.getSuspendContext();
                    DebugProcessImpl debugProcess = context.getDebugProcess();
                    SuspendContextImpl pausedContext = debugProcess.getSuspendManager().getPausedContext();
                    // 2. 通过调试进程获取当前命中的断点
                    // 注意：具体实现可能因不同的调试器而异

                }
            }
        };
        currentSession.addSessionListener(mySessionListener);

    }


}
