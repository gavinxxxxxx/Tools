//package me.gavin.tools.gesture
//
//import android.app.ActivityManager
//import android.app.AppOpsManager
//import android.app.Application
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.net.Uri
//import android.os.Binder
//import android.os.Build
//import android.provider.Settings
//import androidx.core.content.edit
//import androidx.core.content.getSystemService
//import me.gavin.di.instanceHolder
//import me.gavin.util.RxBus
//import me.gavin.util.toast
//import me.gavin.util.toastError
//import org.jetbrains.anko.withAlpha
//import kotlin.math.roundToInt
//
//object FloatingExt {
//
//    var floatingState = instanceHolder.get<SharedPreferences>()
//            .getBoolean("quote_floating_state", false)
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putBoolean("quote_floating_state", value)
//            }
//        }
//    var floatingStable = instanceHolder.get<SharedPreferences>()
//            .getBoolean("quote_floating_stable", false)
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putBoolean("quote_floating_stable", value)
//            }
//        }
//    var floatingShowType = instanceHolder.get<SharedPreferences>()
//            .getString("quote_floating_show_type", "展开显示")
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putString("quote_floating_show_type", value)
//            }
//        }
//    var floatingRate = instanceHolder.get<SharedPreferences>()
//            .getString("quote_floating_rate", "默认")
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putString("quote_floating_rate", value)
//            }
//        }
//    var floatingTextSize = instanceHolder.get<SharedPreferences>()
//            .getString("quote_floating_text_size", "中")
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putString("quote_floating_text_size", value)
//            }
//        }
//    var floatingAlpha = instanceHolder.get<SharedPreferences>()
//            .getInt("quote_floating_alpha", -0xCC)
//        set(value) {
//            field = value
//            instanceHolder.get<SharedPreferences>().edit {
//                putInt("quote_floating_alpha", value)
//            }
//        }
//    val floatingAlphaExt get() = if (floatingAlpha <= 0) "默认" else "${(floatingAlpha / 2.55f).roundToInt()}%"
//    val floatingBgColor get() = 0x000000.withAlpha(if (floatingAlpha <= 0) 0xCC else floatingAlpha)
//
//    val quoteIds by lazy {
//        instanceHolder.get<SharedPreferences>()
//                .getString("quote_floating_quotes", "")!!
//                .split(", ")
//                .mapNotNull { s ->
//                    runCatching {
//                        s.indexOf('_').let {
//                            s.substring(0, it).toLong() to s.substring(it + 1)
//                        }
//                    }.getOrNull()
//                }
//                .let {
//                    linkedSetOf(*it.toTypedArray())
//                }
//    }
//    val quoteCountExt get() = if (quoteIds.isEmpty()) "未选择" else "${quoteIds.size}条"
//    fun quotesCommit() {
//        instanceHolder.get<SharedPreferences>().edit {
//            putString("quote_floating_quotes", quoteIds.joinToString { "${it.first}_${it.second}" })
//        }
//    }
//
//    fun checkFloatingPermission(context: Context): Boolean {
//        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            runCatching {
//                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//                val checkOp = appOps.javaClass.getMethod("checkOp", Int::class.java, Int::class.java, String::class.java)
//                checkOp.isAccessible = true
//                val result = checkOp.invoke(appOps, 24, Binder.getCallingUid(), context.packageName)
//                result == AppOpsManager.MODE_ALLOWED
//            }.getOrDefault(false)
//        } else {
//            Settings.canDrawOverlays(context)
//        }
//    }
//
//    fun toSetting(context: Context) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            "该功能需要悬浮窗权限".toastError()
//        } else {
//            "该功能需要悬浮窗权限".toast()
//            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
//                data = Uri.parse("package:${context.packageName}")
//            })
//        }
//    }
//
//    fun isFloatingServiceRunning(): Boolean {
//        return instanceHolder.get<Application>().getSystemService<ActivityManager>()!!
//                .getRunningServices(30)
//                .any {
//                    it.service.className == FloatingService::class.java.name
//                }
//    }
//
//    val canFloatingShow get() = checkFloatingPermission(App.app) && floatingState
//
//    fun startFloatingService() {
//        if (!checkFloatingPermission(App.app)) {
//            toSetting(App.app)
//        } else {
//            App.app.startService(Intent(App.app, FloatingService::class.java))
//        }
//    }
//
//    fun startFloatingServiceIfShould() {
//        if (checkFloatingPermission(App.app) && floatingState && quoteIds.isNotEmpty()) {
//            App.app.startService(Intent(App.app, FloatingService::class.java))
//        }
//    }
//
//    fun stopFloatingService() {
//        App.app.stopService<FloatingService>()
//    }
//
//    fun notifyQuoteChange() {
//        if (isFloatingServiceRunning()) {
//            RxBus.post(FloatingQuoteChangeEvent())
//        } else {
//            startFloatingServiceIfShould()
//        }
//    }
//}