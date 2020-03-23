package net.avantica.whereisthetruck.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.configuration_fragment.*
import net.avantica.whereisthetruck.R
import net.avantica.whereisthetruck.model.Route


class ConfigurationFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() =
            ConfigurationFragment()
    }

    private lateinit var viewModel: ConfigurationViewModel

    private val loadingObserver = Observer<Boolean> {
        progressBar.visibility = if(it) View.VISIBLE else View.INVISIBLE
    }

    private  val onErrorObserver = Observer<String?> {
        val message = it ?: getString(R.string.generic_error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    private val routesLoadedObserver = Observer<List<Route>> { routes ->
        for (route in routes) {
            val button = RadioButton(context)
            button.id = View.generateViewId()
            button.text = route.name
            button.setOnClickListener {
                val index = routesSelector.indexOfChild(it)
                viewModel.selectRoute(index)
            }

            button.isChecked = route.isFavorite
            routesSelector.addView(button)
        }
    }

    private  val onRouteSelectedObserver = Observer<Route> { result ->
        googleMap?.clear()
        val decodedRoute = PolyUtil.decode(result.route)
        googleMap?.addPolyline(PolylineOptions().addAll(decodedRoute))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(decodedRoute[decodedRoute.count() / 2], 15f))
        scheduleTextview.text = result.schedule
        scheduleTextview.visibility = View.VISIBLE
    }


    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView: View =
            inflater.inflate(R.layout.configuration_fragment, container, false)
        mapView =
            rootView.findViewById<View>(R.id.mapView) as MapView
        mapView?.onCreate(savedInstanceState)
        mapView?.onResume() // needed to get the map to display immediately
        mapView?.getMapAsync(this)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConfigurationViewModel::class.java)

        viewModel.isLoading.observe(this, loadingObserver)
        viewModel.onRoutesDownloaded.observe(this, routesLoadedObserver)
        viewModel.onRouteSelected.observe(this, onRouteSelectedObserver)
        viewModel.onErrorGettingRoutes.observe(this, onErrorObserver)
        viewModel.getRoutes()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
}
