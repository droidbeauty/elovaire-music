# Project-specific ProGuard rules.

-dontwarn java.awt.image.BufferedImage
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.stream.ImageInputStream

# SMBJ keeps optional desktop expression-language and GSS authentication paths
# in the same artifact. Elovaire uses its SMB2/3 NTLM path on Android.
-dontwarn javax.el.**
-dontwarn org.ietf.jgss.**

# jaudiotagger clones tag frame/data objects through public copy constructors.
# R8 cannot see those reflective calls, so keep constructors while allowing normal obfuscation.
-keepclassmembers,allowobfuscation class org.jaudiotagger.** {
    public <init>(...);
}
