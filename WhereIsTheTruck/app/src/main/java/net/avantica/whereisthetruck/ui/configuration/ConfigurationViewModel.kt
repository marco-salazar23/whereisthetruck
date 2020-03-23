package net.avantica.whereisthetruck.ui.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import net.avantica.whereisthetruck.model.FavoriteRoute
import net.avantica.whereisthetruck.model.Route
import net.avantica.whereisthetruck.utilities.FirestoreConstants


class ConfigurationViewModel : ViewModel() {

    val onRoutesDownloaded by lazy { MutableLiveData<List<Route>>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }
    val onRouteSelected by lazy {  MutableLiveData<Route>() }
    val onErrorGettingRoutes by lazy { MutableLiveData<String?>() }

    private var selectedRoute: Route? = null
    private var favoriteRoute: FavoriteRoute? = null
    private var routes = listOf<Route>()
    private val db = Firebase.firestore

    fun getRoutes() {
        isLoading.value = true

        getDefaultRoute {
            db.collection(FirestoreConstants.Tables.ROUTES)
                .get()
                .addOnSuccessListener { result ->
                    var list = mutableListOf<Route>()
                    for (document in result) {
                        var route = document.toObject(Route::class.java)
                        route.id = document.id
                        if (favoriteRoute?.route == route.id) {
                            route.isFavorite = true
                            onRouteSelected.value = route //PolyUtil.decode(route.route)
                        }
                        list.add(route)
                    }
                    isLoading.value = false
                    routes = list
                    onRoutesDownloaded.value = routes
                }
                .addOnFailureListener { exception ->
                    isLoading.value = false
                    onErrorGettingRoutes.value = exception.localizedMessage
                }
        }

    }

    fun selectRoute(atIndex: Int) {
        selectedRoute = routes[atIndex]
        onRouteSelected.value = selectedRoute
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser!!

        val newFavorite = hashMapOf(
            FirestoreConstants.Columns.USER to user.uid,
            FirestoreConstants.Columns.ROUTE to selectedRoute!!.id
        )
        if (favoriteRoute == null) {
            db.collection(FirestoreConstants.Tables.FAVORITES)
                .add(newFavorite)
        } else {
            favoriteRoute!!.route = selectedRoute!!.id
            favoriteRoute!!.id?.let {
                db.collection(FirestoreConstants.Tables.FAVORITES)
                    .document(it)
                    .set(newFavorite)
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
                if (!it.isEmpty) {
                    val document = it.documents.first()
                    favoriteRoute = document.toObject(FavoriteRoute::class.java)
                    favoriteRoute?.id = document.id
                }
                onCompletion()
            }
            .addOnFailureListener {
                onCompletion()
            }
    }
}