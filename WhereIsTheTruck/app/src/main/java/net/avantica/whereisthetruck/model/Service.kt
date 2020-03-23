package net.avantica.whereisthetruck.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import net.avantica.whereisthetruck.utilities.FirestoreConstants
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class Service(
    var route: List<GeoPoint>? = null,
    var start: Timestamp? = null,
    var end: Timestamp? = null,
    @PropertyName("status")
    private var serviceStatus: Long? = null
) {
    val status: FirestoreConstants.ServiceStatus?
    get() {
        return serviceStatus?.toInt()?.let { FirestoreConstants.ServiceStatus.fromInt(it) }
    }

    val startTime: String
    get() {
        val date = start?.toDate()
        date?.let {
            val sfd = SimpleDateFormat(
                "'Inicio' EEEE dd 'a las' hh:mm a",
                Locale.getDefault()
            )
            return sfd.format(it)
        }
        return ""
    }
}