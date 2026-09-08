const val ANDROID_TEST_CLASS_PROPERTY = "app.androidTestClass"
const val ROOM_QUERY_PLAN_TEST_CLASS =
    "elovaire.music.droidbeauty.app.data.library.db.RoomQueryPlanQualificationTest"

fun normalizedInstrumentationTestClass(value: String?): String? {
    return value?.trim()?.takeIf(String::isNotBlank)
}
