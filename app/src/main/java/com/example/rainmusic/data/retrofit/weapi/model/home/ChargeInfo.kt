package com.example.rainmusic.data.retrofit.weapi.model.home


import com.google.gson.annotations.SerializedName

data class ChargeInfo(
    @SerializedName("chargeMessage")
    val chargeMessage: Any,
    @SerializedName("chargeType")
    val chargeType: Int,
    @SerializedName("chargeUrl")
    val chargeUrl: Any,
    @SerializedName("rate")
    val rate: Int
)