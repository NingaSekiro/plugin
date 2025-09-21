//package org.aopbuddy.plugin.action;
//
//import com.intellij.debugger.engine.DebugProcessImpl;
//import com.intellij.debugger.engine.SuspendContext;
//import com.intellij.debugger.engine.SuspendContextImpl;
//import com.intellij.debugger.impl.DebuggerUtilsImpl;
//import com.intellij.debugger.ui.breakpoints.Breakpoint;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.project.Project;
//import com.sun.jdi.*;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MyRemoteDebugAction extends AnAction {
//    private static final String TARGET_CLASS = "your.target.TargetClass"; // e.g., "com.example.Target"
//    private static final int BREAKPOINT_LINE = 42; // 目标行号
//    private static final String HOST = "127.0.0.1";
//    private static final int PORT = 5005;
//
//    // 本地jar文件路径
//    private static final String LOCAL_JAR_PATH = "lib/aopbuddy-1.0-jar-with-dependencies.jar";
//
//    @Override
//    public void actionPerformed(AnActionEvent e) {
//        // 这里可以添加触发远程调试的逻辑
//    }
//
//    public void breakpointReached(Breakpoint bp, SuspendContext ctx) {
//        DebugProcessImpl debugProcess = (DebugProcessImpl) ctx.getDebugProcess();
//        VirtualMachine vm = debugProcess.getVirtualMachineProxy().getVirtualMachine();
//        ThreadReference thread = ((SuspendContextImpl) ctx).getThread().getThreadReference();
//
//        try {
//            // 获取项目根路径
//            String projectBasePath = debugProcess.getProject().getBasePath();
//            String fullJarPath = projectBasePath + "/" + LOCAL_JAR_PATH;
//
//            // 1. 读取本地jar文件
//            byte[] localBytes = Files.readAllBytes(Paths.get(fullJarPath));
//            ArrayReference remoteBytes = (ArrayReference) debugProcess.getVirtualMachineProxy().mirrorOf(localBytes);
//
//            // 2. 获取远程服务器的用户主目录路径
//            List<ReferenceType> systemClasses = vm.classesByName("java.lang.System");
//            if (systemClasses.isEmpty()) {
//                System.err.println("无法找到 java.lang.System 类");
//                return;
//            }
//            ReferenceType systemClass = systemClasses.get(0);
//            List<Method> getMethods = systemClass.methodsByName("getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
//            if (getMethods.isEmpty()) {
//                System.err.println("无法找到 System.getProperty 方法");
//                return;
//            }
//            Method getPropertyMethod = getMethods.get(0);
//            StringReference userHomeKey = debugProcess.getVirtualMachineProxy().mirrorOf("user.home");
//
//            List<Value> args = new ArrayList<Value>();
//            args.add(userHomeKey);
//            Value userHomeValue = ((ClassType) systemClass).invokeMethod(thread, getPropertyMethod, args, ObjectReference.INVOKE_SINGLE_THREADED);
//
//            String userHome = ((StringReference) userHomeValue).value();
//            String remoteJarPath = userHome + "/aopbuddy-1.0-jar-with-dependencies.jar";
//
//            // 3. 构造 Path 对象 (Paths.get(remoteJarPath))
//            List<ReferenceType> pathsClasses = vm.classesByName("java.nio.file.Paths");
//            if (pathsClasses.isEmpty()) {
//                System.err.println("无法找到 java.nio.file.Paths 类");
//                return;
//            }
//            ReferenceType pathsClass = pathsClasses.get(0);
//            List<Method> getMethodsList = pathsClass.methodsByName("get", "(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;");
//            if (getMethodsList.isEmpty()) {
//                System.err.println("无法找到 Paths.get 方法");
//                return;
//            }
//            Method getMethod = getMethodsList.get(0);
//            StringReference pathStr = debugProcess.getVirtualMachineProxy().mirrorOf(remoteJarPath);
//            ArrayReference emptyArray = debugProcess.getVirtualMachineProxy().mirrorOf(new String[0]);
//
//            List<Value> pathArgs = new ArrayList<Value>();
//            pathArgs.add(pathStr);
//            pathArgs.add(emptyArray);
//            Value pathObj = ((ClassType) pathsClass).invokeMethod(thread, getMethod, pathArgs, ObjectReference.INVOKE_SINGLE_THREADED);
//
//            // 4. 调用 Files.write(Path, byte[])
//            List<ReferenceType> filesClasses = vm.classesByName("java.nio.file.Files");
//            if (filesClasses.isEmpty()) {
//                System.err.println("无法找到 java.nio.file.Files 类");
//                return;
//            }
//            ReferenceType filesClass = filesClasses.get(0);
//            List<Method> writeMethods = filesClass.methodsByName("write", "(Ljava/nio/file/Path;[B)Ljava/nio/file/Path;");
//            if (writeMethods.isEmpty()) {
//                System.err.println("无法找到 Files.write 方法");
//                return;
//            }
//            Method writeMethod = writeMethods.get(0);
//
//            List<Value> writeArgs = new ArrayList<Value>();
//            writeArgs.add(pathObj);
//            writeArgs.add(remoteBytes);
//            ((ClassType) filesClass).invokeMethod(thread, writeMethod, writeArgs, ObjectReference.INVOKE_SINGLE_THREADED);
//
//            System.out.println("远程 Files.write 调用完成！文件已传输到: " + remoteJarPath);
//        } catch (IOException e) {
//            System.err.println("IO异常: " + e.getMessage());
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            System.err.println("中断异常: " + e.getMessage());
//            e.printStackTrace();
//        } catch (InvocationException e) {
//            System.err.println("调用异常: " + e.getMessage());
//            e.printStackTrace();
//        } catch (ClassNotLoadedException e) {
//            System.err.println("类未加载异常: " + e.getMessage());
//            e.printStackTrace();
//        } catch (IncompatibleThreadStateException e) {
//            System.err.println("线程状态不兼容异常: " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.err.println("其他异常: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}