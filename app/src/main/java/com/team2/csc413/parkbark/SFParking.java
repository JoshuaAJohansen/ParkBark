package com.team2.csc413.parkbark;

import android.graphics.Color;
import android.location.Location;
import android.util.JsonReader;
import android.util.Log;

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
 * handles the calling to SF Park and retrieving of parking location information
 *
 * @author Aaron Waterman
 */
public class SFParking {

    // creates a unique instance of SFParking
    public static SFParking service = new SFParking();

    // tag is used for debugging with Log.d(TAG, message)
    private static final String TAG = SFParking.class.getSimpleName();

    // data to be stored from Json request
    private String status,
            avail_update_t,
            avail_request_t;
    private int num_records;
    private List<ParkingPlace> parkingList;

    // instantiating default values
    private SFParking() {
        status = null;
        avail_request_t=null;
        avail_update_t=null;
        num_records=0;
        parkingList=null;
    }

    /**
     * returns the response that indicates a success or failure to connect to SF Park
     *
     * @return status   returns "SUCCESS" or "ERROR"
     */
    public String getStatus() {
        return status;
    }

    /**
     * returns a list of parking location with the information collected from SF Park
     *
     * @return parkingList  returns an instance of ArrayList<ParkingPlace>
     */
    public List getParkingList() {
        return parkingList;
    }

    /**
     * returns the number of parking locations retrieved from SF Park
     *
     * @return num_records  default value is 0
     */
    public int getNum_records() { return num_records; }

    /**
     * returns the timestamp of when the availability data response was updated for the request
     *
     * @return avail_update_t
     */
    public String getAvail_update_t() {
        return avail_update_t;
    }

    /**
     * returns timestamp of when the associated request was originally received by SFMTA
     *
     * @return avail_request_t
     */
    public String getAvail_request_t() { return avail_request_t; }

    /**
     * retreives a list of parking locations from SF Park
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
     * handles the creation of the input stream and feeds the input into a Json Reader
     *
     * @param in                            return type InputStream, created from a URL instance
     * @return readParkingList(JsonReader)  method returns an instance of ArrayList<ParkingPlace>
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
     * handles the creation of the url that is used to request information from SF Park
     *
     * @param loc   parses the lattitude and longitude of a location instance
     * @return url  returns the url used to make a request to SF Park
     */
    private URL createURL(Location loc) {
        String url_string;
        double lat, lon;

        lat = loc.getLatitude();
        lon = loc.getLongitude();

        url_string = "http://api.sfpark.org/sfpark/rest/availabilityservice?lat="+lat+"&long="+lon
                + "&radius=200&uom=mile&response=json&pricing=yes";

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
     * reads information from SF Park and then parses information from SF Park
     *
     * @param reader        this is an instance of Json Reader used to parse information collected
     *                      from SF Park
     * @return parking      this is an instance of ArrayList<ParkingPlace>
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

                    if (name.equals("NUM_RECORDS")) {
                        num_records = reader.nextInt();

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
     * reads the AVL array from SF Park and parses the information
     *
     * @param reader                this is an instance of Json Reader used to parse information
     *                              collected from SF Park
     * @return new ParkingPlace     creates and returns a new instance of ParkingPlace containing
     *                              information collected from SF Park
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
     * returns a list of operating hours for the available parking locations within SF Park
     *
     * @param reader        this is an instance of Json Reader used to parse information collected
     *                      from SF Park
     * @return ophrsList    returns an ArrayList<OPHRS> containing the operational hours schedule of
     *                      a particular parking location, this information is stored in an instance
     *                      of ParkingPlace
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
     * returns a list of Rates relating to the available parking locations within SF Park
     *
     * @param reader        this is an instance of Json Reader used to parse information collected
     *                      from SF Park
     * @return rates        returns an ArrayList<Rate> containing one or several different rates of
     *                      a particular parking location, this information is stored in an instance
     *                      of ParkingPlace
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
                if (check2) {
                    reader.endObject();
                }
                rates.add(new Rate(beg, end, rate, desc, rq, rr));
            }
            if (check1) {
                reader.endArray();
            }
        } else {
            Log.d(TAG, "JSON retrieval error: Rate does not contain RS");
        }
        reader.endObject();

        return rates;
    }

    /*
     * ParkingPlace is a class that is used to store data retrieved from SF Park
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

        /**
         * constructor for class ParkingPlace
         *
         * @param type  specifies whether on or off street parking
         * @param name  name of parking location or street with from and to address
         * @param desc  usually address for the parking location
         * @param inter usually cross street intersection parking location
         * @param tel   contact telephone number for parking location, if available
         * @param bfid  unique SFMTA Identifier for on street block face parking
         * @param occ   number of spaces currently occupied
         * @param oper  number of spaces currently operational
         * @param pts   number of location points returned
         * @param ophrs ArrayList<OPHRS> that indicates the operating hours schedule information
         *              for this location
         * @param rates ArrayList<Rate> that indicate the general pricing or rate information
         *              for this location
         * @param loc   location of available parking, contains one point for a lot, garage,
         *              or singular place for parking, or contains two points for street parking
         */
        private ParkingPlace(String type, String name, String desc, String inter, String tel,
                             int bfid, int occ, int oper, int pts, List<OPHRS> ophrs,
                             List<Rate> rates, LocationSFP loc ) {
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

        /**
         * returns a string that specifies whether on or off street parking
         *
         * @return type     specifies whether on or off street parking
         */
        public String getType() {
            return type;
        }

        /**
         *  returns the name of parking location or street with from and to address
         *
         * @return name     name of parking location or street with from and to address
         */
        public String getName() {
            return name;
        }

        /**
         * returns the address for the parking location. This is not always available.
         *
         * @return desc     usually address for the parking location
         */
        public String getDescription() { return desc; }

        /**
         * returns the cross street intersection parking location. This is not always available.
         *
         * @return inter    usually cross street intersection parking location
         */
        public String getIntersection() { return inter; }

        /**
         * returns contact telephone number for parking location. This is not always available.
         *
         * @return tel  contact telephone number for parking location, if available
         */
        public String getTelNum() { return tel; }

        /**
         * returns an instance of LocationSFP that contains two pairs for lattitude and longitude.
         * Be sure to check the number of locations before using secondary pair of lattitude and
         * longitude.
         *
         * @return loc  location of available parking, contains one point for a lot, garage,
         *              or singular place for parking, or contains two points for street parking
         */
        public LocationSFP getLoc() { return loc; }

        /**
         * returns the unique SFMTA Identifier for on street block face parking
         *
         * @return bfid     unique SFMTA Identifier for on street block face parking
         */
        public int getBfid() {
            return bfid;
        }

        /**
         * returns the number of spaces currently occupied
         *
         * @return occ  number of spaces currently occupied
         */
        public int getOccSpaces() {
            return occ;
        }

        /**
         * returns the number of spaces currently operational for this location
         *
         * @return oper     number of spaces currently operational
         */
        public int getOperSpaces() {
            return oper;
        }

        /**
         * returns the number of locations provided by SF Park.
         * The number will be either 1 or 2
         *
         * @return pts      number of location points returned
         */
        public int getNumLocations() {
            return pts;
        }

        /**
         * returns ArrayList<OPHRS> that indicate the operating hours schedule information
         * for this location
         *
         * @return ophrs    ArrayList<OPHRS> that indicates the operating hours schedule information
         *                  for this location
         */
        public List getOperHours() { return ophrs; }

        /**
         * returns ArrayList<Rate> that indicate the general pricing or rate
         * information for this location
         *
         * @return rates    ArrayList<Rate> that indicate the general pricing or rate information
         *                  for this location
         */
        public List getRates() {
            return rates;
        }
    }

    /*
     * OPHRS stores information about operational hours of parking places from SF Park
     */
    public class OPHRS {
        private String from,
                       to,
                       beg,
                       end;

        /**
         * creates an instance of the operational hours schedule information for this location
         *
         * @param from  start day for this schedule
         * @param to    end day for this schedule
         * @param beg   beginning time for this schedule
         * @param end   the end time for this schedule
         */
        protected OPHRS (String from, String to, String beg, String end) {
            this.from = from;
            this.to = to;
            this.beg = beg;
            this.end = end;
        }

        /**
         * returns the start day for this schedule
         *
         * @return from     start day for this schedule
         */
        public String getFrom() {
            return from;
        }

        /**
         * returns the end day for this schedule
         *
         * @return to       end day for this schedule
         */
        public String getTo() {
            return to;
        }

        /**
         * returns the beginning time for this schedule
         *
         * @return beg      beginning time for this schedule
         */
        public String getBeg() {
            return beg;
        }

        /**
         * returns the end time for this schedule
         *
         * @return end  the end time for this schedule
         */
        public String getEnd() {
            return end;
        }
    }

    /**
     * class for storing information about the rates of different parking places from SF Park
     */
    public class Rate {
        private String beg,
                       end,
                       rate,
                       desc,
                       rateQualifier,
                       rateRestriction;

        /**
         * instantiates Rate instance with default values
         *
         * @param beg               begin time for this rate schedule
         * @param end               end time for this rate schedule
         * @param rate              applicable rate for this rate schedule
         * @param desc              the descriptive rate information
         * @param rateQualifier     rate qualifier for this rate schedule
         * @param rateRestriction   rate restriction for this rate schedule
         */
        protected Rate(String beg, String end, String rate, String desc,
                       String rateQualifier, String rateRestriction) {
            this.beg = beg;
            this.end = end;
            this.rate = rate;
            this.desc = desc;
            this.rateQualifier = rateQualifier;
            this.rateRestriction = rateRestriction;
        }

        /**
         * returns the beginning time for this rate schedule
         *
         * @return beg      begin time for this rate schedule
         */
        public String getBeg() {
            return beg;
        }

        /**
         * returns the end time for this rate schedule
         *
         * @return end      end time for this rate schedule
         */
        public String getEnd() {
            return end;
        }

        /**
         * returns the rate for this rate schedule
         *
         * @return rate     applicable rate for this rate schedule
         */
        public String getRate() {
            return rate;
        }

        /**
         * used for descriptive rate information when it is not possible to specify using BEG
         * or END times for this rate schedule
         *
         * @return desc     the descriptive rate information
         */
        public String getDesc() {
            return desc;
        }

        /**
         * returns the rate qualifier for this rate schedule, ex: Per Hr
         *
         * @return rateQualifier        rate qualifier for this rate schedule
         */
        public String getRateQualifier() {
            return rateQualifier;
        }

        /**
         * returns the rate restriction for this rate schedule, if any
         *
         * @return rateRestriction      rate restriction for this rate schedule
         */
        public String getRateRestriction() {
            return rateRestriction;
        }

    }

    /**
     * handles parsing of location information from SF Park
     */
    public class LocationSFP {
        private double latprime, lngprime,
                       latsecond, lngsecond;
        private int numberLocations;

        /**
         * constructor for a location for a ParkingPlace
         *
         * @param location_s    a string parsed from SF Park indicating the location of a particular
         *                      parking place, longitude and latitude are delineated by ","
         */
        protected LocationSFP(String location_s) {
            String[] latlng = location_s.split(",");
            numberLocations = latlng.length;

            if(numberLocations > 2) {
                lngprime = Double.parseDouble(latlng[0]);
                latprime = Double.parseDouble(latlng[1]);
                lngsecond = Double.parseDouble(latlng[2]);
                latsecond = Double.parseDouble(latlng[3]);

            } else {
                lngprime = Double.parseDouble(latlng[0]);
                latprime = Double.parseDouble(latlng[1]);
                lngsecond = -1;
                latsecond = -1;
            }
        }

        /**
         * returns the primary latitude of the location
         *
         * @return latprime     primary latitude, if there is only one location then use this
         *                      method
         */
        public double getLatPrime() { return latprime; }

        /**
         * returns the primary longitude of the location
         *
         * @return lngprime     primary longitude, if there is only one location then use this
         *                      method
         */
        public double getLngPrime() {
            return lngprime;
        }

        /**
         * returns the secondary latitude of the location
         *
         * @return latsecond    secondary latitude, if there is only one location then this method
         *                      returns -1
         */
        public double getLatSecond() {
            return latsecond;
        }

        /**
         * returns a longitude of the location
         *
         * @return lngsecond    secondary longitude, if there is only one location then this method
         *                      returns -1
         */
        public double getLngSecond() {
            return lngsecond;
        }

        /**
         * returns the number of locations stored in this instance
         *
         * @return numberLocations    returns 1 for one location and 2 for two locations
         */
        public int getNumberLocations() { return numberLocations; }
    }

    /**
     * draws the parking information onto a map using information from SF Park
     *
     * @param map   an instance of GoogleMap to be passed from the main activity class
     */
    public void drawParking(GoogleMap map) {

        // sets location to Downtown San Francisco
        Location location = new Location("");
        location.setLatitude(37.792275);
        location.setLongitude(-122.397089);

        service.retrieveParkingList(location);

        List park_li = SFParking.service.getParkingList();

        if (service.getStatus().equals("SUCCESS")) {

            for (int i = 0; i < service.getNum_records(); i++) {

                ParkingPlace place = (ParkingPlace) park_li.get(i);

                LocationSFP locsfp = place.getLoc();

                double  latstart = locsfp.getLatPrime(),
                        latend = locsfp.getLatSecond(),
                        lngstart = locsfp.getLngPrime(),
                        lngend = locsfp.getLngSecond();

                LatLng start = new LatLng(latstart, lngstart);
                LatLng end = new LatLng(latend, lngend);
                LatLng mid = new LatLng( (latstart + latend) / 2 ,
                                         (lngstart + lngend) / 2 );

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
                            .snippet(createSnippet(place))
                            .alpha(0.2f)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE)));

                } else {
                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(place.getName())
                            .position(start)
                            .snippet(createSnippet(place)));
                }
            }
        } else {
            Log.d(null, "Application can not connect to SF Parking service");
        }
    }

    /**
     * creates a string containing information about the rate and available parking spaces of a
     * particular parking place from SF Park
     *
     * @param place     a parking place retrieved from SF Park
     * @return snippet  indicates the cost of the parking space and if there are spaces available
     */
    private String createSnippet(ParkingPlace place) {
        String snippet = "";

        // display rate and available spaces
        if (place.getRates() != null) {
            List rates = place.getRates();

            Rate rate = (Rate) rates.get(0);

            if ( !rate.getRate().equals("0") ) {
                snippet += "$" + rate.getRate() + " " + rate.getRateQualifier() + " ";
            } else {
                snippet += rate.getRateQualifier() + " ";
            }
        }
        if (place.getOccSpaces() >= 0) {
            snippet += (place.getOperSpaces() - place.getOccSpaces()) + " available spaces";
        }

        return snippet;
    }
}
