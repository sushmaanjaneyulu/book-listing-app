package com.example.android.booklistingapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import static com.example.android.booklistingapp.ListingActivity.LOG_TAG;

public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractItemFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of volume info (or books).
            JSONArray itemsArray = null;
            if (baseJsonResponse.has("items")) {
                itemsArray = baseJsonResponse.getJSONArray("items");


                // For each book / item in the itemArray, create an {@link Book} object
                for (int i = 0; i < itemsArray.length(); i++) {

                    // Get a single book /item at position i within the list of books
                    JSONObject currentItem = itemsArray.getJSONObject(i);

                    // For a given book /item, extract the JSONObject associated with the
                    // key called "properties", which represents a list of all properties
                    // for that earthquake.
                    JSONObject volumeInfo = currentItem.getJSONObject("volumeInfo");

                    // Extract the value for the key called "title"
                    String title = "No title";
                    if (volumeInfo.has("title")) {
                        title = volumeInfo.getString("title");
                    }

                    // Extract authors JSONArray associated with the key called "authors"
                    // which may represents a list of authors of the book
                    JSONArray authorsArray;
                    StringBuilder authors = new StringBuilder();
                    if (volumeInfo.has("authors")) {
                        authorsArray = volumeInfo.getJSONArray("authors");
                        // List each item in the authors array
                        for (int j = 0; j < authorsArray.length(); j++) {
                            authors.append(System.getProperty("line.separator"));
                            authors.append(authorsArray.getString(j));
                        }
                    } else {
                        authors.append("No Author");
                    }

                    // Extract the value for the key called "description"
                    String description = "No Description";
                    if (volumeInfo.has("description")) {
                        description = volumeInfo.getString("description");
                    }

                    // Extract categories JSONArray associated with the key called "categories"
                    // which may represents a list of authors of the book
                    JSONArray categoriesArray;
                    StringBuilder categories = new StringBuilder();
                    String firstCategory = "";
                    char firstLetter = 'n';
                    String firstLetterAsString = "N";
                    if (volumeInfo.has("categories")) {
                        categoriesArray = volumeInfo.getJSONArray("categories");
                        // List each item in the categories array
                        for (int k = 0; k < categoriesArray.length(); k++) {
                            categories.append(System.getProperty("line.separator"));
                            categories.append(categoriesArray.getString(k));
                        }
                        firstCategory = categoriesArray.get(0).toString().toLowerCase();
                        Log.v(LOG_TAG, "First Category: " + firstCategory);
                        firstLetter = firstCategory.charAt(0);
                        Log.v(LOG_TAG, "First Category Letter: " + firstLetter);
                        firstLetterAsString = String.valueOf(firstLetter).toUpperCase();
                        Log.v(LOG_TAG, "First Category Letter String: " + firstLetterAsString);
                    } else {
                        categories.append("No Category");
                        firstCategory = "No Category";
                    }

                    // Extract the value for the key called "canonicalVolumeLink"
                    String url = "https://books.google.co.uk";
                    if (volumeInfo.has("canonicalVolumeLink")) {
                        url = volumeInfo.getString("canonicalVolumeLink");
                    }


                    // Create a new {@link Book} object with the title, location, description,
                    // and url from the JSON response.
                    Book book = new Book(title, authors, categories, firstLetterAsString, description, url);

                    // Add the new {@link Book} to the list of books.
                    books.add(book);
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the book JSON results", e);
        }

        // Return the list of earthquakes
        return books;
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
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
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
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
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
     * Query the Google books dataset and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "Fetch book data");
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> books = extractItemFromJson(jsonResponse);

        // Return the list of {@link Books}s
        return books;
    }

}
