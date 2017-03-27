package com.example.android.yrestaurants;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kevin on 3/16/2017.
 */

public class QueryUtils {

    private static final String LOG_TAG = "QueryUtils";
    private static final String CLIENT_ID = "kIcyDnfdqfG7S9KvU8QHfA";
    private static final String CLIENT_SECRET = "MCpwYGVV3N8WFsfIy4Fg3wRXXBijd4nXGHkahI9z2rmZKiFZkjmXgwqpdqGJwKLi";
    private static final String GRANT_TYPE = "client_credentials";

    private QueryUtils() {
    }

    public static String getBearerToken(String requestUrl){
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequestForToken(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP POST request.", e);
        }

        String bearerToken = null;
        try {
            // Create a JSONObject from the JSON response string
            JSONObject json = new JSONObject(jsonResponse);
            bearerToken = json.getString("access_token");
        } catch(JSONException e){
            Log.e(LOG_TAG, "Problem parsing the restaurant JSON results", e);
        }

        return bearerToken;
    }

    /**
     * Make an HTTP POST request to the given URL and return a String as the response.
     */
    private static String makeHttpRequestForToken(URL url) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoOutput(true);

            String param = "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
            "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8")+
            "&grant_type=" + URLEncoder.encode(GRANT_TYPE, "UTF-8");

            // Send post request
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(param);
            wr.flush();
            wr.close();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Restaurant JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequestForRestaurant(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
//        Log.v(LOG_TAG, jsonResponse);
        return jsonResponse;

    }


    public static List<Restaurant> fetchRestaurantData(String requestUrl, String token) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequestForRestaurant(url, token);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP GET request.", e);
        }


        // Extract relevant fields from the JSON response and create a list of {@link Restaurant}s
        List<Restaurant> Restaurants = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Restaurant}s
        return Restaurants;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP GET request to the given URL and return a String as the response.
     */
    private static String makeHttpRequestForRestaurant(URL url, String token) throws IOException {
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Restaurant JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequestForRestaurant(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Restaurant} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Restaurant> extractFeatureFromJson(String restaurantJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(restaurantJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding restaurants to
        List<Restaurant> restaurants = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(restaurantJSON);

            // Extract the JSONArray associated with the key called "businesses",
            // which represents a list of businesses.
            JSONArray businessesArray = baseJsonResponse.getJSONArray("businesses");

            // For each businesses in the businessesArray, create an {@link business} object
            for (int i = 0; i < businessesArray.length(); i++) {
                try {
                    // Get a single restaurant at position i within the list of businesses
                    JSONObject currentRestaurant = businessesArray.getJSONObject(i);


                    // Extract the value for the key called "rating" "phone" etc...
                    int rating = currentRestaurant.getInt("rating");
                    String phone = currentRestaurant.getString("phone");
                    String name = currentRestaurant.getString("name");
                    String url = currentRestaurant.getString("url");
                    String image_url = currentRestaurant.getString("image_url");
                    String id = currentRestaurant.getString("id");
                    boolean is_closed = currentRestaurant.getBoolean("is_closed");
                    int reviewCount = currentRestaurant.getInt("review_count");
                    int distance = currentRestaurant.getInt("distance");
                    JSONArray categories = currentRestaurant.getJSONArray("categories");
                    String category = categories.getJSONObject(0).getString("title");

                    // Create a new {@link restaurant} object
                    Restaurant restaurant = new Restaurant(name, url, image_url, phone,
                            is_closed, rating, reviewCount, distance, category, id);

                    // Add the new {@link restaurant} to the list of restaurants.
                    restaurants.add(restaurant);
                } catch (JSONException e){
                    Log.e(LOG_TAG, "something is missing in this restaurant", e);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the restaurant JSON results", e);
        }

        // Return the list of restaurants
        return restaurants;
    }

}
