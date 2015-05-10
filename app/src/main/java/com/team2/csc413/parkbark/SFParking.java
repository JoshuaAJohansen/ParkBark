package com.team2.csc413.parkbark;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Created by Aaron Waterman on 5/10/2015.
 */
public class SFParking {

    // creates a unique instance of SFParking
    public static SFParking service = new SFParking();

    // tag is used for debugging with Log.d(TAG, message)
    private static final String TAG = SFParking.class.getSimpleName();

    // data to be stored from Json request
    private String status,
            message,
            avail_update_t,
            avail_request_t;
    private int requestID,
        udf1,
        num_records;
    private List<ParkingPlace> parkingList;

    // instantiating default values
    private SFParking() {
        status = null;
        message=null;
        avail_request_t=null;
        avail_update_t=null;
        requestID=-1;
        udf1=-1;
        num_records=0;
        parkingList=null;
    }


    public String getStatus() {
        return status;
    }

    public List getParkingList() {
        return parkingList;
    }

    public int getRequestID() {
        return requestID;
    }

    public int getUdf1() {
        return udf1;
    }

    public int getNum_records() { return num_records; }

    public String getMessage() { return message; }

    public String getAvail_update_t() {
        return avail_update_t;
    }

    public String getAvail_request_t() { return avail_request_t; }

    /**
     * returns a list of parking locations
     *
     * @param loc
     * @throws java.io.IOException
     */
    public void retrieveParkingList(Location loc) {
        URL serviceURL = createURL(loc);

        try {
            parkingList = readStreamSFP(serviceURL.openStream());
        }
        catch(IOException io_e) {
            Log.d(TAG, io_e.toString());
        }

    }

    /**
     * returns ArrayList<ParkingPlace> from the Json input stream
     *
     * @param in
     * @return java.util.List
     * @throws IOException
     */
    private List readStreamSFP(InputStream in) throws IOException {

        InputStreamReader isr = new InputStreamReader(in, "UTF-8");

        JsonReader reader = new JsonReader(isr);

        try {
            return readParkingList(reader);
        }
        finally{
            reader.close();
        }
    }

    /**
     * creates a URL object from a location.
     *
     * @param loc
     * @return java.net.URL
     */
    private URL createURL(Location loc) {
        String url_string;
        double lat, lon;

        lat = loc.getLatitude();
        lon = loc.getLongitude();

        url_string = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat="+lat+"&long="+lon
                + "&radius=200&uom=mile&response=json";

        try {
            URL url = new URL(url_string);
            return url;
        }
        catch (MalformedURLException url_e) {
            Log.d(TAG, url_e.toString());

            return null;
        }
    }

    /**
     * reads information from SF Park, determining if the connection was a success
     * then parses information from SF Park
     *
     * @param reader
     * @return java.util.List
     * @throws IOException
     */
    private List readParkingList(JsonReader reader) throws IOException {
        List parking = new ArrayList();

        reader.beginObject();
        String name = reader.nextName();

        if (name.equals("STATUS")) {

            status = reader.nextString();

            if (status.equals("SUCCESS")) {

                while(reader.hasNext()) {
                    name = reader.nextName();

                    if (name.equals("REQUESTID")) {
                        requestID = reader.nextInt();

                    } else if (name.equals("UDF1")) {
                        udf1 = reader.nextInt();

                    } else if (name.equals("NUM_RECORDS")) {
                        num_records = reader.nextInt();

                    } else if (name.equals("MESSAGE")) {
                        message = reader.nextString();

                    } else if (name.equals("AVAILABILITY_UPDATED_TIMESTAMP")) {
                        avail_update_t = reader.nextString();

                    } else if (name.equals("AVAILABILITY_REQUEST_TIMESTAMP")) {
                        avail_request_t = reader.nextString();

                    } else if (name.equals("AVL")) {
                        reader.beginArray();

                        while (reader.hasNext()) {
                            parking.add(readAVL(reader));
                        }

                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }

                }

            } else if (status.equals("ERROR")) {
                status = name;
                Log.d(TAG, reader.nextString());
            } else {
                Log.d(TAG,
        "readParkingList(JsonReader) : \"SUCCESS\" and \"ERROR\" were not found within \"STATUS\"");
            }

        } else {
            Log.d(TAG, "readParkingList(JsonReader) : STATUS not found");
        }

        return parking;
    }

    /**
     * creates a list of parking location from the AVL array within the SF Park Json
     *
     * @param reader
     * @return new Parking Place
     * @throws IOException
     */
    private ParkingPlace readAVL(JsonReader reader) throws IOException {
        String reader_s;

        String type = null,
               name = null,
               desc = null,
               inter = null,
               tel = null;
        int bfid = -1,
            occ = -1,
            oper = -1,
            pts = -1;
        LocationSFP loc = null;
        List<OPHRS> ophrs = null;
        List<Rate> rates = null;

        reader.beginObject();

        while(reader.hasNext()) {
            reader_s = reader.nextName();

            if (reader_s.equals("TYPE")) {
                type = reader.nextString();

            } else if (reader_s.equals("NAME")) {
                name = reader.nextString();

            } else if (reader_s.equals("DESC")) {
                desc = reader.nextString();

            }else if (reader_s.equals("INTER")) {
                inter = reader.nextString();

            } else if (reader_s.equals("TEL")) {
                tel = reader.nextString();

            } else if (reader_s.equals("BFID")) {
                bfid = reader.nextInt();

            } else if (reader_s.equals("OCC")) {
                occ = reader.nextInt();

            } else if (reader_s.equals("OPER")) {
                oper = reader.nextInt();

            } else if (reader_s.equals("PTS")) {
                pts = reader.nextInt();

            } else if (reader_s.equals("LOC")) {
                loc = new LocationSFP( reader.nextString() );

            } else if (reader_s.equals("OPHRS")) {
                ophrs = readOPHRS(reader);

            } else if (reader_s.equals("RATES")) {
                rates = readRates(reader);

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new ParkingPlace(type, name, desc, inter, tel, bfid, occ, oper, pts, ophrs, rates, loc);
    }


    /**
     * returns a list of operating hours for the available parking location
     *
     * @param reader
     * @return ArrayList<OPHRS>
     * @throws IOException
     */
    private List readOPHRS(JsonReader reader) throws IOException {
        List<OPHRS> ophrsList = new ArrayList<OPHRS>();

        String from = null,
               to = null,
               beg = null,
               end = null,
               name;

        reader.beginObject();

        name = reader.nextName();
        if (name.equals("OPS")) {

            android.util.JsonToken test = reader.peek();
            boolean check1 = test.name().equals("BEGIN_ARRAY");

            if (check1) {
                reader.beginArray();
            }

            while (reader.hasNext()) {

                test = reader.peek();
                boolean check2 = test.name().equals("BEGIN_OBJECT");

                if (check2) {
                    reader.beginObject();
                }

                while (reader.hasNext()) {

                    name = reader.nextName();

                    if (name.equals("FROM")) {
                        from = reader.nextString();

                    } else if (name.equals("TO")) {
                        to = reader.nextString();

                    } else if (name.equals("BEG")) {
                        beg = reader.nextString();

                    } else if (name.equals("END")) {
                        end = reader.nextString();

                    } else {
                        reader.skipValue();
                    }
                }

                if (check2) {
                    reader.endObject();
                }

                ophrsList.add(new OPHRS(from, to, beg, end));
            }

            if (check1) {
                reader.endArray();
            }

        } else {
            Log.d(TAG, "JSON retrieval error: Rate does not contain RS");
        }

        reader.endObject();

        return ophrsList;
    }

    /**
     * returns an list of Rates relating to the available parking location
     *
     * @param reader
     * @return ArrayList<Rate>
     * @throws IOException
     */
    private List readRates(JsonReader reader) throws IOException {
        List<Rate> rates = new ArrayList<Rate>();

        String beg = null,
               end = null,
               rate = null,
               desc = null,
               rq = null,
               rr = null,
               name;

        reader.beginObject();

        name = reader.nextName();

        if (name.equals("RS")) {

            android.util.JsonToken test = reader.peek();
            boolean check1 = test.name().equals("BEGIN_ARRAY");

            if (check1) {
                reader.beginArray();
            }

            while (reader.hasNext()) {

                test = reader.peek();
                boolean check2 = test.name().equals("BEGIN_OBJECT");

                if (check2) {
                    reader.beginObject();
                }

                while (reader.hasNext()) {
                    name = reader.nextName();

                    if (name.equals("BEG")) {
                        beg = reader.nextString();

                    } else if (name.equals("END")) {
                        end = reader.nextString();

                    } else if (name.equals("RATE")) {
                        rate = reader.nextString();

                    } else if (name.equals("DESC")) {
                        desc = reader.nextString();

                    } else if (name.equals("RQ")) {
                        rq = reader.nextString();

                    } else if (name.equals("RR")) {
                        rr = reader.nextString();

                    } else {
                        reader.skipValue();
                    }
                }

                reader.endObject();

                rates.add(new Rate(beg, end, rate, desc, rq, rr));
            }
            reader.endArray();

        } else {
            Log.d(TAG, "JSON retrieval error: Rate does not contain RS");
        }
        reader.endObject();

        return rates;
    }

    /*
     * ParkingPlace helps store data retrieved from SF Park
     */
    public class ParkingPlace {
        private String type,
                       name,
                       desc,
                       inter,
                       tel;
        private int bfid,
                    occ,
                    oper,
                    pts;
        private LocationSFP loc;
        private List<OPHRS> ophrs;
        private List<Rate> rates;

        private ParkingPlace(String type, String name, String desc, String inter, String tel, int bfid, int occ,
                                  int oper, int pts, List<OPHRS> ophrs, List<Rate> rates, LocationSFP loc ) {
            this.type = type;
            this.name = name;
            this.desc = desc;
            this.inter = inter;
            this.tel = tel;
            this.bfid = bfid;
            this.occ = occ;
            this.oper = oper;
            this.pts = pts;
            this.ophrs = ophrs;
            this.rates = rates;
            this.loc = loc;

        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() { return desc; }

        public String getIntersection() { return inter; }

        public String getTelNum() { return tel; }

        public LocationSFP getLoc() { return loc; }

        public int getBfid() {
            return bfid;
        }

        public int getOccSpaces() {
            return occ;
        }

        public int getOperSpaces() {
            return oper;
        }

        public int getNumLocations() {
            return pts;
        }

        public List getOperHours() { return ophrs; }

        public List getRates() {
            return rates;
        }
    }

    /*
     * operational hours
     */
    public class OPHRS {
        private String from,
                       to,
                       beg,
                       end;

        protected OPHRS (String from, String to, String beg, String end) {
            this.from = from;
            this.to = to;
            this.beg = beg;
            this.end = end;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getBeg() {
            return beg;
        }

        public String getEnd() {
            return end;
        }
    }

    /*
     * rates
     */
    public class Rate {
        private String beg,
                       end,
                       rate,
                       desc,
                       rateQualifier,
                       rateRestriction;

        protected Rate(String beg, String end, String rate, String desc, String rateQualifier, String rateRestriction) {
            this.beg = beg;
            this.end = end;
            this.rate = rate;
            this.desc = desc;
            this.rateQualifier = rateQualifier;
            this.rateRestriction = rateRestriction;
        }

        public String getBeg() {
            return beg;
        }

        public String getEnd() {
            return end;
        }

        public String getRate() {
            return rate;
        }

        public String getDesc() {
            return desc;
        }

        public String getRateQualifier() {
            return rateQualifier;
        }

        public String getRateRestriction() {
            return rateRestriction;
        }

    }

    public class LocationSFP {
        //private int numloc;

        private double lat1, lng1,
               lat2, lng2;

        protected LocationSFP(String location_s) {
            String[] latlng = location_s.split(",");

            if(latlng.length > 2) {
                lng1 = Double.parseDouble(latlng[0]);
                lat1 = Double.parseDouble(latlng[1]);
                lng2 = Double.parseDouble(latlng[2]);
                lat2 = Double.parseDouble(latlng[3]);
                //numloc = 2;
            } else {
                lng1 = Double.parseDouble(latlng[0]);
                lat1 = Double.parseDouble(latlng[1]);
                lng2 = -1;
                lat2 = -1;
                //numloc = 1;
            }
        }

        public double getLat1() {
            return lat1;
        }
        public double getLng1() {
            return lng1;
        }
        public double getLat2() {
            return lat2;
        }
        public double getLng2() {
            return lng2;
        }
        /*
        public int getNumLocations() {
            return numloc;
        }
        */
    }

    /*
     * makes a request to SF Park and draws the parking locaions on the map
     */
    public void drawParking( GoogleMap map /*, LayoutInflater inflater */) {
        Location location = new Location("");
        location.setLatitude(37.792275);
        location.setLongitude(-122.397089);

        service.retrieveParkingList(location);
        List park_li = SFParking.service.getParkingList();

        if (service.getStatus().equals("SUCCESS")) {

            for (int i = 0; i < service.getNum_records(); i++) {

                ParkingPlace place = (ParkingPlace) park_li.get(i);

                LocationSFP locsfp = place.getLoc();

                String information = "";

                double latstr = locsfp.getLat1(),
                        latend = locsfp.getLat2(),
                        lngstr = locsfp.getLng1(),
                        lngend = locsfp.getLng2();

                LatLng start = new LatLng(latstr, lngstr);
                LatLng end = new LatLng(latend, lngend);
                LatLng mid = new LatLng( (latstr + latend) / 2 ,
                        (lngstr + lngend) / 2 );

                if (place.getDescription() != null) {
                    information += place.getDescription() + "\n";
                }

                if (place.getIntersection() != null) {
                    information += place.getIntersection() + "\n";
                }

                if (place.getType() != null) {
                    information += place.getType().toLowerCase() + " street parking\n";
                }

                if (place.getOperHours() != null) {

                    List ophr = place.getOperHours();

                    for (int j=0; j < ophr.size(); j++) {
                        OPHRS ophrs = (OPHRS) ophr.get(j);

                        information += "From: " + ophrs.getFrom() + "\nTo: " + ophrs.getTo()
                                + "Time: " + ophrs.getBeg() + " to " + ophrs.getEnd() + "\n";
                    }
                }

                if (place.getTelNum() != null) {
                    information += "phone #: " + place.getTelNum() + "\n";
                }

                if (place.getRates() != null) {
                    Rate r = (Rate) place.getRates();

                    if (r.getDesc() != null) {
                        information += r.getDesc()
                                + "\nrate: " + r.getRate() + " " + r.getRateQualifier()
                                + "\nrate restriction: " + r.getRateRestriction();

                    } else {
                        information += "begins: " + r.getBeg() + "\nends: " + r.getEnd()
                                + "\nrate: " + r.getRate() + " " + r.getRateQualifier()
                                + "\nrate restriction: " + r.getRateRestriction();
                    }
                }

                if (place.getOccSpaces() >= 0) {
                    information += "parking spaces occupied: "
                            + place.getOccSpaces() + "\n";
                }

                if (place.getOperSpaces() >= 0) {
                    information += "parking spaces operational: "
                            + place.getOperSpaces() + "\n";
                }

                if (place.getOccSpaces() >= 0 && place.getOperSpaces() >= 0) {
                    information += "parking spaces open: "
                            + (place.getOperSpaces() - place.getOccSpaces()) + "\n";
                }

                if (place.getNumLocations() > 1) {
                    Polyline polyline = map.addPolyline(new PolylineOptions()
                            .add(start, end)
                            .color(Color.BLUE)
                            .width(7)
                            .visible(true)
                            .geodesic(true));

                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(place.getName())
                            .position(mid)
                            .snippet(information)
                            .alpha(0.2f)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE)));

                    //map.setInfoWindowAdapter(new StreetInfoWindow(
                    //        inflater));

                } else {
                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(place.getName())
                            .position(start)
                            .snippet(information));
                }
            }
        } else {
            Log.d(null, "Application can not connect to SF Parking service");
        }
    }
    /*
    class StreetInfoWindow implements GoogleMap.InfoWindowAdapter {
        private final View markerView;

        LayoutInflater inflater;

        StreetInfoWindow(LayoutInflater inflater) {
            this.inflater = inflater;

            markerView = inflater
                    .inflate(R.layout.custom_info_contents, null);
        }
        @Override
        public View getInfoWindow(Marker marker) {

            return null;
        }
        @Override
        public View getInfoContents(Marker marker) {
            TextView information = (TextView) markerView.findViewById(R.id.title);
            information.setText(marker.getTitle());

            information = (TextView) markerView.findViewById(R.id.information);
            information.setText(marker.getSnippet());

            return markerView;
        }

    }
    */

}
