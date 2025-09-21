package org.aopbuddy.plugin.service

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

// 模拟接口调用（挂起函数，非阻塞）
suspend fun fetchUserData(userId: String): String {
    println("开始获取用户数据（协程：${Thread.currentThread().name}）")
    delay(1000) // 挂起1秒（非阻塞，释放线程）
    return "用户$userId 的数据"
}

// 模拟处理数据（挂起函数）
suspend fun processData(data: String): String {
    println("开始处理数据（协程：${Thread.currentThread().name}）")
    delay(500) // 挂起0.5秒
    return "处理后的数据：$data"
}

fun main() = runBlocking(Dispatchers.Default) { // 启动主协程（使用默认调度器）
    println("主线程/主协程开始（线程：${Thread.currentThread().name}）")

    // 启动一个协程执行异步任务（非阻塞）
    launch {
        println("异步任务开始（协程：${Thread.currentThread().name}）")
        
        // 非阻塞暂停：等待用户数据（期间释放线程）
        val userData = fetchUserData("1001")

        // 非阻塞暂停：等待数据处理（期间释放线程）
        val processedData = processData(userData)
        
        println("异步任务完成：$processedData（协程：${Thread.currentThread().name}）")
    }

    // 主协程不阻塞，继续执行自己的逻辑
    println("主协程继续执行其他任务...")
    for (i in 1..3) {
        TimeUnit.MILLISECONDS.sleep(300) // 模拟主协程的工作（非阻塞）
        println("主协程完成第 $i 项工作（线程：${Thread.currentThread().name}）")
    }
}