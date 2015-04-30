package com.team2.csc413.parkbark;

import android.location.Location;
import android.location.LocationManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Created by Michael on 4/28/15.
 */
public class SFParking {

    LocationManager locationmanager;
    Location location;
    String serviceURL = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat=37.785990&long=-122.411362&radius=0.25&uom=mile&response=json";
    String apiReturn;
    String name;
    String response = "";

    /**
     * Reads the parking information from SFPark website
     * @param current Current location
     * @throws IOException
     */
    public void readSFPark(Location current) throws IOException {
        /*LocationManager locationmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(serviceURL);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        apiReturn = EntityUtils.toString(httpEntity);

        try {
            response = "";
            parse();
        } catch (JSONException e) {
            apiReturn = e.getMessage();
        }
    }

    /**
    * @return The parking information of current location
    */
    public String getParkingInfo() {
        return response;
    }

    public void parse() throws JSONException {
        JSONObject object = new JSONObject(apiReturn);
        JSONArray jsonArray = object.getJSONArray("AVL");
        for(int x=0; x<jsonArray.length(); x++){
            JSONObject jsonItem = jsonArray.getJSONObject(x);
            name = jsonItem.getString("NAME");
            if (response == "") response = response + name;
            response = response + "\n" + name;
        }
    }
}