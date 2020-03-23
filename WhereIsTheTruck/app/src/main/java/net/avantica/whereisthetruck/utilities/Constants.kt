package net.avantica.whereisthetruck.utilities

object FirestoreConstants {
    object Tables {
        const val ROUTES = "routes"
        const val FAVORITES = "favorites"
        const val SERVICES = "services"
        const val SERVICE = "service"
    }

    object Columns {
        const val USER = "user"
        const val ROUTE = "route"
        const val STATUS = "status"
    }

     enum class ServiceStatus(val status: Int) {
        INACTIVE(0), ACTIVE(1);

         companion object {
             fun fromInt(value: Int) = ServiceStatus.values().first { it.ordinal == value }
         }
    }

}

const val IMAGE_UPDATED_BROADCAST = "Profile image updated"

