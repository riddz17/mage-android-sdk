package mil.nga.giat.mage.sdk.http.get;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.location.Location;
import mil.nga.giat.mage.sdk.datastore.observation.Observation;
import mil.nga.giat.mage.sdk.datastore.observation.ObservationHelper;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeature;
import mil.nga.giat.mage.sdk.gson.deserializer.LayerDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.LocationDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.jackson.FeatureDeserializer;
import mil.nga.giat.mage.sdk.gson.deserializer.jackson.ObservationDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;
import mil.nga.giat.mage.sdk.utils.DateUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

/**
 * A class that contains common GET requests to the MAGE server.
 * 
 * @author travis
 * 
 */
public class MageServerGetRequests {

    private static final String LOG_NAME = MageServerGetRequests.class.getName();
    private static ObservationDeserializer observationDeserializer = new ObservationDeserializer();
    private static FeatureDeserializer featureDeserializer = new FeatureDeserializer();

    /**
     * Gets layers from the server.
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static List<Layer> getFeatureLayers(Context context) {
        final Gson layerDeserializer = LayerDeserializer.getGsonBuilder();
        List<Layer> layers = new ArrayList<Layer>();
        DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
        HttpEntity entity = null;
        try {
            Uri uri = Uri.parse(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey)).buildUpon()
                .appendPath("api")
                .appendPath("layers")
                .appendQueryParameter("type", "Feature")
                .build();

            HttpGet get = new HttpGet(uri.toString());
            HttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                JSONArray featureArray = new JSONArray(EntityUtils.toString(entity));
                for (int i = 0; i < featureArray.length(); i++) {
                    JSONObject feature = featureArray.getJSONObject(i);
                    if (feature != null) {
                        layers.add(layerDeserializer.fromJson(feature.toString(), Layer.class));
                    }
                }
			} else {
				String error = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "Failure parsing layer information.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        return layers;
    }

    public static List<Layer> getStaticLayers(Context context) {
        final Gson layerDeserializer = LayerDeserializer.getGsonBuilder();
        List<Layer> layers = new ArrayList<Layer>();
        DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
        HttpEntity entity = null;
        try {
            
            Uri uri = Uri.parse(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey)).buildUpon()
                .appendPath("api")
                .appendPath("layers")
                .appendQueryParameter("type", "External")
                .build();

            HttpGet get = new HttpGet(uri.toString());
            HttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                JSONArray featureArray = new JSONArray(EntityUtils.toString(entity));
                for (int i = 0; i < featureArray.length(); i++) {
                    JSONObject feature = featureArray.getJSONObject(i);
                    if (feature != null) {
                        layers.add(layerDeserializer.fromJson(feature.toString(), Layer.class));
                    }
                }
			} else {
				String error = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "Failure parsing layer information.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        return layers;
    }

    /**
     * Makes a GET request to the MAGE server for the Field Observation Layer
     * Id.
     * 
     * @param context
     * @return
     */
    public static String getFieldObservationLayerId(Context context) {
        String fieldObservationLayerId = null;
        List<Layer> layers = MageServerGetRequests.getFeatureLayers(context);
        for (Layer layer : layers) {
            fieldObservationLayerId = layer.getRemoteId();
        }
        
        return fieldObservationLayerId;
    }

//    public static Collection<StaticFeature> getStaticFeatures(Context context, Layer pLayer) {
//        long start = 0;
//
//        Collection<StaticFeature> staticFeatures = new ArrayList<StaticFeature>();
//        HttpEntity entity = null;
//        try {
//            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
//            final Gson staticFeatureDeserializer = StaticFeatureDeserializer.getGsonBuilder();
//
//            URL staticFeatureURL = new URL(serverURL, "/FeatureServer/" + pLayer.getRemoteId() + "/features");
//            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
//            Log.d(LOG_NAME, staticFeatureURL.toString());
//            HttpGet get = new HttpGet(staticFeatureURL.toURI());
//            HttpResponse response = httpclient.execute(get);
//
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                entity = response.getEntity();
//                start = System.currentTimeMillis();
//
//                JSONObject json = new JSONObject(EntityUtils.toString(entity));
//
//                if (json != null && json.has("features")) {
//                    JSONArray features = json.getJSONArray("features");
//                    for (int i = 0; i < features.length(); i++) {
//                        JSONObject feature = (JSONObject) features.get(i);
//                        if (feature != null) {
//                            StaticFeature staticFeature = staticFeatureDeserializer.fromJson(feature.toString(), StaticFeature.class);
//                            staticFeature.setLayer(pLayer);
//                            staticFeatures.add(staticFeature);
//                        }
//                    }
//                }
//            } else {
//                String error = EntityUtils.toString(response.getEntity());
//                Log.e(LOG_NAME, "Bad request.");
//                Log.e(LOG_NAME, error);
//            }
//        } catch (Exception e) {
//            // this block should never flow exceptions up! Log for now.
//            Log.e(LOG_NAME, "There was a failure while retriving static features.", e);
//        } finally {
//            try {
//                if (entity != null) {
//                    entity.consumeContent();
//                }
//            } catch (Exception e) {
//                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
//            }
//        }
//        
//        long stop = System.currentTimeMillis();
//        
//        Log.i("observations", "JACKSON staticFeatures " + staticFeatures.size() + " took " + (stop - start) + " millis");
//        return staticFeatures;
//    }
    
    public static Collection<StaticFeature> getStaticFeatures(Context context, Layer pLayer) {
        long start = 0;

        Collection<StaticFeature> staticFeatures = new ArrayList<StaticFeature>();
        HttpEntity entity = null;
        try {
            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

            URL staticFeatureURL = new URL(serverURL, "/FeatureServer/" + pLayer.getRemoteId() + "/features");
            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
            Log.d(LOG_NAME, staticFeatureURL.toString());
            HttpGet get = new HttpGet(staticFeatureURL.toURI());
            HttpResponse response = httpclient.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();
                start = System.currentTimeMillis();
                staticFeatures = featureDeserializer.parseObservations(entity.getContent());
            } else {
				String error = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "There was a failure while retriving static features.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        
        long stop = System.currentTimeMillis();
        
        Log.i("observations", "JACKSON staticFeatures " + staticFeatures.size() + " took " + (stop - start) + " millis");
        return staticFeatures;
    }

    /**
     * Returns the observations from the server. Uses a date as in filter in the
     * request.
     * 
     * @param context
     * @return
     */
    public static List<Observation> getObservations(Context context) {  
        long start = 0;
        
        List<Observation> observations = new ArrayList<Observation>();
        String fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
        HttpEntity entity = null;
        try {
            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));

            ObservationHelper observationHelper = ObservationHelper.getInstance(context);

            // TODO : should we add one millisecond to this?
            Date lastModifiedDate = observationHelper.getLatestRemoteLastModified();

            URL observationURL = new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features");
            Uri.Builder uriBuilder = Uri.parse(observationURL.toURI().toString()).buildUpon();
            uriBuilder.appendQueryParameter("startDate", DateUtility.getISO8601().format(lastModifiedDate));

            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
            Log.d(LOG_NAME, uriBuilder.build().toString());
            HttpGet get = new HttpGet(new URI(uriBuilder.build().toString()));
            HttpResponse response = httpclient.execute(get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				start = System.currentTimeMillis();
				observations = observationDeserializer.parseObservations(entity.getContent());
			} else {
				String error = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            // this block should never flow exceptions up! Log for now.
            Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
        } finally {
            try {
                if (entity != null) {
                    entity.consumeContent();
                }
            } catch (Exception e) {
                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
            }
        }
        long stop = System.currentTimeMillis();
        
        Log.i("observations", "JACKSON observations " + observations.size() + " took " + (stop - start) + " millis");
        
        return observations;
    }
//    
//    public static List<Observation> getObservations(Context context) {
//        long start = 0;
//
//        List<Observation> observations = new ArrayList<Observation>();
//        String fieldObservationLayerId = MageServerGetRequests.getFieldObservationLayerId(context);
//        HttpEntity entity = null;
//        try {
//            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
//
//            ObservationHelper observationHelper = ObservationHelper.getInstance(context);
//
//            // TODO : should we add one millisecond to this?
//            Date lastModifiedDate = observationHelper.getLatestRemoteLastModified();
//
//            final Gson observationDeserializer = ObservationDeserializer.getGsonBuilder();
//
//            URL observationURL = new URL(serverURL, "/FeatureServer/" + fieldObservationLayerId + "/features");
//            Uri.Builder uriBuilder = Uri.parse(observationURL.toURI().toString()).buildUpon();
//            uriBuilder.appendQueryParameter("startDate", DateUtility.getISO8601().format(lastModifiedDate));
//
//            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
//            Log.d(LOG_NAME, uriBuilder.build().toString());
//            HttpGet get = new HttpGet(new URI(uriBuilder.build().toString()));
//            HttpResponse response = httpclient.execute(get);
//
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                entity = response.getEntity();
//                start = System.currentTimeMillis();
//                JSONObject json = new JSONObject(EntityUtils.toString(entity));
//
//                if (json != null && json.has("features")) {
//                    JSONArray features = json.getJSONArray("features");
//                    for (int i = 0; i < features.length(); i++) {
//                        JSONObject feature = (JSONObject) features.get(i);
//                        if (feature != null) {
//                            observations.add(observationDeserializer.fromJson(feature.toString(), Observation.class));
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // this block should never flow exceptions up! Log for now.
//            Log.e(LOG_NAME, "There was a failure while performing an Observation Fetch opperation.", e);
//        } finally {
//            try {
//                if (entity != null) {
//                    entity.consumeContent();
//                }
//            } catch (Exception e) {
//                Log.w(LOG_NAME, "Trouble cleaning up after GET request.", e);
//            }
//        }
//        
//        long stop = System.currentTimeMillis();
//        
//        Log.i("observations", "GSON observations " + observations.size() + " took " + (stop - start) + " millis");
//        return observations;
//    }

    public static Collection<Location> getLocations(Context context) {

        Collection<Location> locations = new ArrayList<Location>();
        final Gson locationDeserializer = LocationDeserializer.getGsonBuilder();

        HttpEntity entity = null;

        try {
            URL serverURL = new URL(PreferenceHelper.getInstance(context).getValue(R.string.serverURLKey));
            URL locationURL = new URL(serverURL, "/api/locations");

            DefaultHttpClient httpclient = HttpClientManager.getInstance(context).getHttpClient();
            // Log.d(LOG_NAME, uriBuilder.build().toString());
            HttpGet get = new HttpGet(locationURL.toURI());
            HttpResponse response = httpclient.execute(get);

			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();

				JSONArray jsonArray = new JSONArray(EntityUtils.toString(entity));
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject user = (JSONObject) jsonArray.get(i);
					JSONArray jsonLocations = user.getJSONArray("locations");
					// concerned w/ the first (most recent) location for now
					if (jsonLocations.length() > 0) {
						JSONObject jsonLocation = (JSONObject) jsonLocations.get(0);
						Location location = locationDeserializer.fromJson(jsonLocation.toString(), Location.class);
						locations.add(location);
					}
				}
			} else {
				String error = EntityUtils.toString(response.getEntity());
				Log.e(LOG_NAME, "Bad request.");
				Log.e(LOG_NAME, error);
			}
        } catch (Exception e) {
            Log.e(LOG_NAME, "There was a failure while performing an Location Fetch opperation.", e);
        }

        return locations;

    }

}
