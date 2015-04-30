package com.team2.csc413.parkbark;

import android.location.Location;
import android.location.LocationManager;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michael on 4/28/15.
 */
public class SFParking {

    LocationManager locationmanager;
    Location location;
    String serviceURL = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat=37.785990&long=-122.411362&radius=0.25&uom=mile&response=json";
    //String apiReturn;
    InputStream is;
    String apiReturn;
    String name[];
    int test;

    /**
     * Reads the parking information from SFPark website
     * @param current Current location
     * @throws IOException
     */
    public void readSFPark(Location current) throws IOException {
        int len = 500;
        /*LocationManager locationmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/

        try{
            /*serviceURL = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat=" +
                    current.getLatitude()+"&long="+current.getLongitude()+"&radius=0.25&uom=mile&response=json";*/
            URL url = new URL(serviceURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            apiReturn = readIt(is, len);
        }catch(IOException e) {
            apiReturn = e.getMessage();
        }finally {
            if(is!=null) is.close();
        }

        /*try {
            parse();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    /**
    * @return The parking information of current location
    */
    public String[] getParkingInfo() {
        //return apiReturn;
        return name;
    }

    public void parse() throws JSONException {
        //JsonReader reader = new JsonReader(is);

        /*JSONObject object = new JSONObject(apiReturn);
        JSONArray jsonArray = object.getJSONArray("AVL");
        for(int x=0; x<jsonArray.length(); x++){
            JSONObject jsonItem = jsonArray.getJSONObject(x);
            name[x] = jsonItem.getString("NAME");
        }*/
    }

}
