package com.aican.tlcanalyzer.domain.model.auth

data class SignUpModel(
    var name: String,
    var email: String,
    var uid: String,
    var userCreationDateTime: String,
    var userCreationDateTimeFormat: String,
    var userCreationDate: String,
    var subscriptionStartDate: String,
    var subscriptionEndDate: String,
    var lastAppAccessDate: String,
    var lastAppAccessTime: String,
    var projectLimit: Int,
    var numberOfUsers: Int,
    var pcode: String

)