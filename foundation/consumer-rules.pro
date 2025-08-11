-dontwarn javax.annotation.**

-keeppackagenames okhttp3.internal.publicsuffix.*
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn java.lang.invoke.StringConcatFactory

-if public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}

-keep public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}

-assumenosideeffects class com.guavapay.paymentsdk.logging.LoggingKt {
    public static void d(...);
    public static void v(...);
}

-keepclassmembers enum * {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.guavapay.paymentsdk.gateway.banking.PaymentMethod { *; }

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload { *; }

-keep class com.guavapay.paymentsdk.gateway.banking.PaymentResult { *; }
-keep class com.guavapay.paymentsdk.gateway.banking.PaymentResult$* { *; }

-keep class com.guavapay.paymentsdk.gateway.banking.GatewayException { *; }
-keep class com.guavapay.paymentsdk.gateway.banking.GatewayException$* { *; }
-keep class com.guavapay.paymentsdk.network.ssevents.SseException { *; }

-keep class com.guavapay.paymentsdk.gateway.** { *; }

-keep class com.guavapay.paymentsdk.network.services.BindingsApi.** { *; }
-keep class com.guavapay.paymentsdk.network.services.OrderApi.** { *; }

-keep class androidx.startup.** { *; }
-keep class * implements androidx.startup.Initializer {
    <init>();
    public void create(android.content.Context);
    public java.util.List dependencies();
}

-keep class com.guavapay.paymentsdk.LibraryZygote { *; }
-keep class com.guavapay.paymentsdk.platform.context.IsolatedInitializer { *; }

-keepnames class * extends androidx.startup.Initializer
-keep class * extends androidx.startup.Initializer {
    <init>();
}

-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class * implements androidx.lifecycle.ViewModelProvider$Factory {
    <methods>;
}

-keepclassmembers class androidx.lifecycle.ViewModelProvider$Factory {
    public <methods>;
}

-keep class com.guavapay.paymentsdk.presentation.platform.ViewModelFactory { *; }

-keep class androidx.lifecycle.ViewModelProvider { *; }
-keep class androidx.lifecycle.viewmodel.CreationExtras { *; }
-keep class androidx.lifecycle.viewmodel.** { *; }

-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keepnames class com.guavapay.paymentsdk.presentation.navigation.PaymentRoutes$* { *; }
-keepnames class com.guavapay.paymentsdk.presentation.navigation.Route { *; }
-keepnames class com.guavapay.paymentsdk.presentation.navigation.Route$* { *; }

-keep class com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM$State$Mode { *; }
