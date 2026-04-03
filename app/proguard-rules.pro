# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.carflow.app.**$$serializer { *; }
-keepclassmembers class com.carflow.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.carflow.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
