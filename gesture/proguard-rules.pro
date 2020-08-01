# ----------------------------------------------------------------------------
# 混淆的压缩比例，0-7
-optimizationpasses 5
# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# 指定混淆是采用的算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# 指定外部模糊字典
-obfuscationdictionary proguard-dic-O0o.txt
# 指定class模糊字典
-classobfuscationdictionary proguard-dic-O0o.txt
# 指定package模糊字典
-packageobfuscationdictionary proguard-dic-O0o.txt
# 忽略警告
#-ignorewarnings
# ----------------------------------------------------------------------------
# 自定义注解混淆过滤
-keep @me.gavin.base.IgnoredOnProguard class * { *; }
-keep class * { @me.gavin.base.IgnoredOnProguard <fields>; }
-keepclassmembers class * { @me.gavin.base.IgnoredOnProguard <methods>; }
# ----------------------------------------------------------------------------