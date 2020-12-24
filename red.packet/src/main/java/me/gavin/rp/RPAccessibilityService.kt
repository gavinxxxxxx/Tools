package me.gavin.rp

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RPAccessibilityService : AccessibilityService() {

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        println("${event.className} - $event")
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            println("TYPE_WINDOW_STATE_CHANGED - ${event.className}")
//        }

        if (!App.state) return

        wx(event)

    }

    private fun AccessibilityNodeInfo.findClickableParent(): AccessibilityNodeInfo? {
        return parent?.let { if (it.isClickable) it else it.findClickableParent() }
    }

    private fun wx(event: AccessibilityEvent) {
        if (event.packageName != "com.tencent.mm") return

        // 打开红包消息
        rootInActiveWindow
            ?.findAccessibilityNodeInfosByText("微信红包")
            ?.mapNotNull { it.findClickableParent() }
            ?.lastOrNull { it.childCount < 3 }
            ?.also { println(it) }
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // 拆红包
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            && event.className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI"
        ) {
            println("打开红包消息了 尝试点击红包")
            handle.postDelayed({
                rootInActiveWindow
                    ?.findAccessibilityNodeInfosByText("开")
                    ?.firstOrNull()
                    ?.also { println("瞧瞧我找到了啥 - $it") }
                    ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    .also {
                        println("打开红包消息了 尝试点击红包 - $it")
                    }
                    ?: let {
                        println("打开红包消息了 红包领完了 - $it")
                        // 红包派完了 直接返回
                        rootInActiveWindow
                            ?.findAccessibilityNodeInfosByText("返回")
                            ?.firstOrNull()
                            ?.also { println("瞧瞧我找到了啥 - $it") }
                            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
            }, 100)
        }

        // 退出红包详情页
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            && event.className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"
        ) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private val handle = Handler()

    private fun AccessibilityNodeInfo.clickTarget(): AccessibilityNodeInfo? {
        return if (isClickable) this else parent?.clickTarget()
    }

}

