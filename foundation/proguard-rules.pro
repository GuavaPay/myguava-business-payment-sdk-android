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

-keep class com.guavapay.paymentsdk.gateway.PaymentGatewayStateParcelable { *; }

-keep class com.guavapay.paymentsdk.gateway.banking.PaymentResult { *; }
-keep class com.guavapay.paymentsdk.gateway.banking.PaymentResult$* { *; }

-keep class com.guavapay.paymentsdk.gateway.** { *; }