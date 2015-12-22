package es.ric.bluetoothgps.nmea;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ricardo on 17/12/15.
 */
public class NMEA {

    // fucking java interfaces
    interface SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position);
    }

    // utils
    static float Latitude2Decimal(String lat, String NS) {
        try {
            float med = Float.parseFloat(lat.substring(2)) / 60.0f;
            med += Float.parseFloat(lat.substring(0, 2));
            if (NS.startsWith("S")) {
                med = -med;
            }
            return med;
        }
        catch(Exception e){
            return 0;
        }
    }

    static float Longitude2Decimal(String lon, String WE) {
        try {
            float med = Float.parseFloat(lon.substring(3)) / 60.0f;
            med += Float.parseFloat(lon.substring(0, 3));
            if (WE.startsWith("W")) {
                med = -med;
            }
            return med;
        }
        catch(Exception e){
            return 0;
        }
    }

    // parsers
    class GPGGA implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {
            try {
                position.time = Float.parseFloat(tokens[1]);
            }
            catch(Exception e){}
            position.lat = Latitude2Decimal(tokens[2], tokens[3]);
            position.lon = Longitude2Decimal(tokens[4], tokens[5]);
            try {
                position.quality = Integer.parseInt(tokens[6]);
            }
            catch(Exception e){}
            try {
                position.altitude = Float.parseFloat(tokens[9]);
            }
            catch(Exception e){}

            return true;
        }
    }

    class GPGGL implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {
            position.lat = Latitude2Decimal(tokens[1], tokens[2]);
            position.lon = Longitude2Decimal(tokens[3], tokens[4]);
            try {
                position.time = Float.parseFloat(tokens[5]);
            }
            catch(Exception e){}
            return true;
        }
    }

    class GPRMC implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {
            try {
                position.time = Float.parseFloat(tokens[1]);
            }
            catch(Exception e){}
            position.lat = Latitude2Decimal(tokens[3], tokens[4]);
            position.lon = Longitude2Decimal(tokens[5], tokens[6]);
            try {
                position.velocity = Float.parseFloat(tokens[7]);
            }
            catch(Exception e){}
            try {
                position.dir = Float.parseFloat(tokens[8]);
            }
            catch(Exception e){}
            return true;
        }
    }

    class GPVTG implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {
            position.dir = Float.parseFloat(tokens[3]);
            return true;
        }
    }

    class GPRMZ implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {
            position.altitude = Float.parseFloat(tokens[1]);
            return true;
        }
    }


    GPSPosition position = new GPSPosition();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<String, SentenceParser>();

    public NMEA() {
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        sentenceParsers.put("GPRMZ", new GPRMZ());
        //only really good GPS devices have this sentence but ...
        sentenceParsers.put("GPVTG", new GPVTG());
    }

    public GPSPosition parse(String line) {

        if(line.startsWith("$")) {
            String nmea = line.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            //TODO check crc
            if(sentenceParsers.containsKey(type)) {
                sentenceParsers.get(type).parse(tokens, position);
            }
            position.updatefix();
        }

        return position;
    }
}