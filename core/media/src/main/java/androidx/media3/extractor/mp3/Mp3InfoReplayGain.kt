package androidx.media3.extractor.mp3

import androidx.media3.common.Metadata

class Mp3InfoReplayGain(
    val peak: Float,
    val field1Name: Byte,
    val field1Originator: Byte,
    val field1Value: Float,
    val field2Name: Byte,
    val field2Originator: Byte,
    val field2Value: Float
) : Metadata.Entry {
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
}
