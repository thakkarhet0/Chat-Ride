package com.example.chatroom.models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.chatroom.MainActivity;
import com.example.chatroom.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapHelper {

    public static final int REQUEST_LOCATION = 72;

    FusedLocationProviderClient mFusedLocationClient;

    MainActivity activity;

    ArrayList<LatLng> mMarkerPoints = new ArrayList<>();

    ArrayList<Marker> markersList = new ArrayList<>();

    LocationManager lm;
    LocationCallback mLocationCallback;
    private Polyline mPolyline;

    public void clearMarkers() {
        markersList.clear();
        mMarkerPoints.clear();
    }

    public MapHelper(MainActivity activity) {
        this.activity = activity;
        lm = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity);
    }

    public void requestLocationPerms() {
        ActivityCompat.requestPermissions(this.activity, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    public boolean hasLocationPerms() {
        return ActivityCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void stopUpdates() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public Marker justAddMarker(GoogleMap mMap, LatLng point, String placeName) {
        MarkerOptions options = new MarkerOptions();
        options.position(point);
        options.title(placeName);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return mMap.addMarker(options);
    }

    public Marker addMarker(GoogleMap mMap, LatLng point, String placeName) {
        return addMarker(mMap, point, placeName, null);
    }

    public Marker addMarker(GoogleMap mMap, LatLng point, String placeName, BitmapDescriptor bit) {
        if (mMarkerPoints.size() > 1) {
            mMarkerPoints.clear();
            mMap.clear();
        }

        mMarkerPoints.add(point);

        MarkerOptions options = new MarkerOptions();

        options.position(point);
        options.title(placeName);

        if (bit != null) {
            options.icon(bit);
        } else {
            if (mMarkerPoints.size() == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else if (mMarkerPoints.size() == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
        }

        Marker marker = mMap.addMarker(options);
        markersList.add(marker);

        decorate(mMap);

        return marker;
    }

    public void updateMarker(GoogleMap map, LatLng point, int pos, Float bearing) {
        Marker marker = markersList.get(pos);
        marker.setPosition(point);
        if (bearing != null) {
            marker.setRotation(bearing);
            marker.setAnchor((float) 0.5, (float) 0.5);
        }
        mMarkerPoints.set(pos, point);
        decorate(map);
    }

    public void decorate(GoogleMap mMap) {
        if (markersList.size() > 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker m : markersList) {
                builder.include(m.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 50;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cameraUpdate);
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(markersList.get(0).getPosition(), 15);
            mMap.animateCamera(cameraUpdate);
        }

        if (mMarkerPoints.size() >= 2) {
            drawRoute(mMap, mMarkerPoints.get(0), mMarkerPoints.get(1));
        }
    }

    public void camUpdate(GoogleMap mMap, LatLng point, int zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, zoom);
        mMap.animateCamera(cameraUpdate);
    }

    @SuppressLint("MissingPermission")
    public boolean getLastLocation(ILastLocation callback) {
        if (hasLocationPerms()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        requestNewLocationData(callback);
                    }
                });
                return true;
            } else {
                sendLocOffMessage();
            }
        } else {
            requestLocationPerms();
        }
        return false;
    }

    public void sendLocOffMessage() {
        Toast.makeText(this.activity, "Please turn your location on!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(ILastLocation callback) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(20);
        mLocationRequest.setFastestInterval(20);
        if (callback.stopAfterOneUpdate())
            mLocationRequest.setNumUpdates(1);
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                callback.onUpdate(locationResult.getLastLocation());
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    public boolean isLocationEnabled() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void drawRoute(GoogleMap nMap, LatLng mOrigin, LatLng mDestination) {
        String url = getDirectionsUrl(mOrigin, mDestination);
        DownloadTask downloadTask = new DownloadTask(nMap, this.activity);
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "key=" + this.activity.getString(R.string.google_maps_key);
        String parameters = str_origin + "&" + str_dest + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public interface ILastLocation {

        void onUpdate(Location location);

        boolean stopAfterOneUpdate();

    }

    public static class DirectionsJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {

                jRoutes = jObject.getJSONArray("routes");

                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString(list.get(l).latitude));
                                hm.put("lng", Double.toString(list.get(l).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        GoogleMap nMap;

        MainActivity activity;

        DownloadTask(GoogleMap nMap, MainActivity activity) {
            this.nMap = nMap;
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(this.nMap, this.activity);

            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        GoogleMap nMap;

        MainActivity activity;

        ParserTask(GoogleMap nMap, MainActivity activity) {
            this.nMap = nMap;
            this.activity = activity;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = this.nMap.addPolyline(lineOptions);

            } else
                Toast.makeText(this.activity, "No route is found", Toast.LENGTH_LONG).show();
        }
    }


}
