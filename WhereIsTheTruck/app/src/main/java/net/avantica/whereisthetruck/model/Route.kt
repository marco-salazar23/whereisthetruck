package net.avantica.whereisthetruck.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Route(var id: String? = null, var name: String? = null, var route: String? = null, var schedule: String? = null, var isFavorite: Boolean = false)