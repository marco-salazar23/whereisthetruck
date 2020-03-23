package net.avantica.whereisthetruck.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_home.*
import net.avantica.whereisthetruck.R
import net.avantica.whereisthetruck.databinding.FragmentHomeBinding
import net.avantica.whereisthetruck.model.Service

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: HomeViewModel
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null


    private val onRouteStartedObserver = Observer<Service?> { service ->
        if (service == null) {
            messageLabel.text = getString(R.string.no_route)
            messageLabel.visibility = View.VISIBLE
        } else {
            messageLabel.visibility = View.GONE
            service.route?.let { points ->
                googleMap?.clear()
                googleMap?.addPolyline(PolylineOptions().addAll(points.map { LatLng(it.latitude, it.longitude) }))
                if (points.isNotEmpty()) {
                    val position  =  LatLng(points.last().latitude, points.last().longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10f))
                }
                val binding: FragmentHomeBinding? = DataBindingUtil.bind(view!!)
                binding?.service = service
                infoLabel.visibility = View.VISIBLE
            }
        }
    }

    private val onNoRouteSelectedObserver = Observer<Unit> {
        messageLabel.text = getString(R.string.no_favorite_route)
        messageLabel.visibility = View.VISIBLE
        infoLabel.visibility = View.GONE
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView: View =
            inflater.inflate(R.layout.fragment_home, container, false)
        mapView =
            rootView.findViewById<View>(R.id.mapView) as MapView
        mapView?.onCreate(savedInstanceState)
        mapView?.onResume() // needed to get the map to display immediately
        mapView?.getMapAsync(this)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.onRouteStarted.observe(this, onRouteStartedObserver)
        viewModel.noFavoriteRoute.observe(this, onNoRouteSelectedObserver)
        viewModel.checkData()
    }

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


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
}
