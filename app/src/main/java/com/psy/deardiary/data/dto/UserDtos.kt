package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName
import com.psy.deardiary.data.model.UserProfile

data class UserProfileResponse(
    val id: Int,
    val email: String,
    val name: String?,
    val bio: String?
)

data class UserProfileUpdateRequest(
    val name: String?,
    val bio: String?
)

fun UserProfileResponse.toUserProfile(): UserProfile {
    return UserProfile(id = id, email = email, name = name, bio = bio)
}
