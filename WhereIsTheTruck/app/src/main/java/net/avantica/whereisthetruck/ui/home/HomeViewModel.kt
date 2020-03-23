package net.avantica.whereisthetruck.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import net.avantica.whereisthetruck.model.FavoriteRoute
import net.avantica.whereisthetruck.model.Service
import net.avantica.whereisthetruck.utilities.FirestoreConstants

class HomeViewModel : ViewModel() {

    val onRouteStarted = MutableLiveData<Service?>()
    val noFavoriteRoute = MutableLiveData<Unit>()

    private val db = Firebase.firestore
    var favoriteRoute: FavoriteRoute? = null

    fun checkData() {
        getDefaultRoute {
            favoriteRoute?.let {
                val docRef =
                    db.collection(FirestoreConstants.Tables.SERVICES)
                        .document("${it.route}")
                        .collection(FirestoreConstants.Tables.SERVICE)
                        .whereEqualTo(
                            FirestoreConstants.Columns.STATUS,
                            FirestoreConstants.ServiceStatus.ACTIVE.ordinal
                        )

                docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    var list = mutableListOf<Service>()
                    snapshot?.documents?.forEach { document ->
                        val service = document.toObject(Service::class.java)
                        service?.let {
                            list.add(it)
                        }
                        onRouteStarted.value = list.firstOrNull()
                    }
                }
            }
        }
    }

    private fun getDefaultRoute(onCompletion: () -> Unit) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser!!

        db.collection(FirestoreConstants.Tables.FAVORITES)
            .whereEqualTo(FirestoreConstants.Columns.USER , user.uid)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    noFavoriteRoute.value = null
                } else {
                    val document = it.documents.first()
                    favoriteRoute = document.toObject(FavoriteRoute::class.java)
                    favoriteRoute?.id = document.id
                    onCompletion()
                }

            }
            .addOnFailureListener {
                onCompletion()
            }
    }
}