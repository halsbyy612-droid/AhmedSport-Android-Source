package com.ahmedsport.app

/**
 * نقطة واحدة لقراءة مصادر القناة. القيم تأتي من stream-urls.properties المحلي
 * عبر BuildConfig، ولا تُكتب داخل ملفات Kotlin أو HTML.
 */
object StreamConfig {
    const val channelName = "قناة أحمد سبورت 1"

    val streamUrls: List<String> by lazy {
        listOf(
            BuildConfig.STREAM_1,
            BuildConfig.STREAM_2,
            BuildConfig.STREAM_3,
            BuildConfig.STREAM_4
        ).filter { it.startsWith("http://") || it.startsWith("https://") }
    }
}
