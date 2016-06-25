package jp.gr.java_conf.daisy.georeminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.SupportMapFragment

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = getSupportFragmentManager().findFragmentById(R.id.map)!! as SupportMapFragment;
        mapFragment.getMapAsync { map ->
            // TODO
        };
    }
}
