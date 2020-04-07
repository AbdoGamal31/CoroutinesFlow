package com.coroutinesflow.features.home.data.model

import android.os.Parcelable
import androidx.room.TypeConverters
import com.coroutinesflow.features.home.data.local_datastore.db.ItemsDataConverter
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comics(
    @SerializedName("available") val available: Int,
    @SerializedName("collectionURI") val collectionURI: String?,
    @TypeConverters(ItemsDataConverter::class)@SerializedName("items") val items: List<Items>?,
    @SerializedName("returned") val returned: Int
) : Parcelable