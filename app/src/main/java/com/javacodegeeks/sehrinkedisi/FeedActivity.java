package com.javacodegeeks.sehrinkedisi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;

import org.w3c.dom.Text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class FeedActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    RadioGroup group1, group2;
    RadioButton mama1, mama2, mama3, gr1, gr2, gr3, gr4;
    String mama = "Seçilmedi";
    String gram = "Seçilmedi";

    boolean true1 = false;
    boolean true2 = false;

    Button bt1;

    TextView major;

    String assets = "asset://beytepe.tif";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    String email;

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    public double latitude, longitude;
    public SymbolManager symbolManager;
    public Symbol symbol;
    CameraPosition position;


    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();




        group1 = (RadioGroup) findViewById(R.id.group1);
        group2 = (RadioGroup) findViewById(R.id.group2);

        mama1 = (RadioButton) findViewById(R.id.kedimama);
        mama2 = (RadioButton) findViewById(R.id.kopekmama);
        mama3 = (RadioButton) findViewById(R.id.yemek);
        gr1 = (RadioButton) findViewById(R.id.elli);
        gr2 = (RadioButton) findViewById(R.id.yuzelli);
        gr3 = (RadioButton) findViewById(R.id.ikiyuzelli);
        gr4 = (RadioButton) findViewById(R.id.besyuz);




        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, "sk.eyJ1IjoiYmFyYW5jdCIsImEiOiJja2pzeG4zY3gwNWVuMnJyZWhuMGF4aDVxIn0.aVSvb1fGLmWBHER_EtG-EQ");

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_feed);
        major = (TextView) findViewById(R.id.majority);

        bt1 = (Button) findViewById(R.id.biraktim);
        bt1.setEnabled(false);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.kedimama:
                if (checked)
                    mama = "Kedi Maması";
                    true1 = true;
                    break;
            case R.id.kopekmama:
                if (checked)
                    mama = "Köpek Maması";
                    true1 = true;
                    break;
            case R.id.yemek:
                if (checked)
                    mama = "Yiyecek";
                    true1 = true;
                break;
            case R.id.elli:
                if (checked)
                    gram = "50 gr";
                    true2 = true;
                break;
            case R.id.yuzelli:
                if (checked)
                    gram = "150 gr";
                    true2 = true;
                break;
            case R.id.ikiyuzelli:
                if (checked)
                    gram = "250 gr";
                    true2 = true;
                break;
            case R.id.besyuz:
                if (checked)
                    gram = "500 gr";
                    true2 = true;
                break;

        }

        if (true1 == true && true2 == true) {

            bt1.setEnabled(true);

        }
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        FeedActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconIgnorePlacement(true);

                        symbol = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(latitude, longitude)).withIconImage(ICON_ID)
                                .withIconSize(2.0f));

                    }
                });


    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            latitude = locationComponent.getLastKnownLocation().getLatitude();
            longitude = locationComponent.getLastKnownLocation().getLongitude();

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            //Zoom Camera
                    position = new CameraPosition.Builder()
                    .target(new LatLng(latitude,longitude))
                    .zoom(10)
                    .tilt(20)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }




    public void button (View view) {

        major.setText("5. Baran CANPOLAT");

        symbolManager.update(symbol);
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/outdoors-v11").withImage(ICON_ID, BitmapFactory.decodeResource(
                FeedActivity.this.getResources(), R.drawable.mapbox_marker_icon_default)));


        HashMap<String, Object> data = new HashMap<>();
        data.put("Lat", latitude);
        data.put("Long", longitude);
        data.put("Tarih ve Saati", FieldValue.serverTimestamp());
        data.put("Mama Çeşidi", mama);
        data.put("Miktarı", gram);
        data.put("User Email", email);

        firebaseFirestore.collection("Datas").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override public void onSuccess(DocumentReference documentReference) {

                Toast.makeText(FeedActivity.this, "Kaydedildi", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {

                Toast.makeText(FeedActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }});

    }

    public void markets(View view) {

        position = new CameraPosition.Builder()
                .target(new LatLng(latitude,longitude)).zoom(15).tilt(20).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/baranct/ckjzvh9h80ob417nlau58ayqc"));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {

            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

