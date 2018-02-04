package com.taptaptap.referrercasestudy.model

data class PostDataModel(
        var userId: String,
        var implmentationid: String,
        var trafficSource: String,
        var userClass: String,
        var deviceId: String?,
        var manufacturer: String?,
        var model: String?
)
