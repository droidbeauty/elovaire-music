package android.net

import android.os.Parcel

class TestUri(
    private val value: String = "content://test",
) : Uri() {
    override fun buildUpon(): Uri.Builder = Uri.Builder()

    override fun getAuthority(): String = "test"

    override fun getEncodedAuthority(): String = authority

    override fun getEncodedFragment(): String? = null

    override fun getEncodedPath(): String = "/"

    override fun getEncodedQuery(): String? = null

    override fun getEncodedSchemeSpecificPart(): String = "//test"

    override fun getEncodedUserInfo(): String? = null

    override fun getFragment(): String? = null

    override fun getHost(): String = "test"

    override fun getLastPathSegment(): String = "test"

    override fun getPath(): String = "/"

    override fun getPathSegments(): List<String> = listOf("test")

    override fun getPort(): Int = -1

    override fun getQuery(): String? = null

    override fun getScheme(): String = "content"

    override fun getSchemeSpecificPart(): String = "//test"

    override fun getUserInfo(): String? = null

    override fun isHierarchical(): Boolean = true

    override fun isRelative(): Boolean = false

    override fun toString(): String = value

    override fun writeToParcel(parcel: Parcel, flags: Int) = Unit

    override fun describeContents(): Int = 0
}
