package net.avantica.whereisthetruck.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class FavoriteRoute(var id: String? = null, var user: String? = null, var route: String? = null)