package me.gavin.rp

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RPAccessibilityService : AccessibilityService() {

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        println("${event.className} - $event")
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            println("TYPE_WINDOW_STATE_CHANGED - ${event.className}")
        }

        if (!App.state) return

        wx(event)

    }

    private fun wx(event: AccessibilityEvent) {
        if (event.packageName != "com.tencent.mm") return

        // 打开红包消息
        rootInActiveWindow
            ?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/atb")
            ?.lastOrNull {
                it.findAccessibilityNodeInfosByText("微信红包").any() && // 是微信红包
                        it.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aul").none() // 已领取/已过期
            }
            ?.also { println(it) }
            ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // 拆红包
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            && event.className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI"
        ) {
            handle.postDelayed({
                rootInActiveWindow
                    ?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dan")
                    ?.firstOrNull()
                    ?.also { println(it) }
                    ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    ?: let {
                        // 红包派完了 直接返回
                        rootInActiveWindow
                            ?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d84")
                            ?.firstOrNull()
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

}

