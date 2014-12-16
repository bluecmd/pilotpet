package nu.cmd.pilotpet.dao.impl;

import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.cmd.pilotpet.dao.MetarDao;
import nu.cmd.pilotpet.models.CloudLayer;
import nu.cmd.pilotpet.models.Metar;
import nu.cmd.pilotpet.models.Station;

/**
 * Created by aa on 12/14/2014.
 */
public class AviationweathergovMetarDao implements MetarDao {

    private class MetarUriBuilder {
        private StringBuilder stringBuilder = new StringBuilder("https://aviationweather.gov" +
                "/adds/dataserver_current/httpparam?");
        private String dataSource = "metars";
        private String requestType = "retrieve";
        private String format = "xml";
        private String stationString;
        private String radialDistance;
        private int hoursBeforeNow = 24;
        private boolean mostRecentForEachStation = true;

        public String build() {
            stringBuilder.append("dataSource=").append(dataSource)
                    .append("&requestType=").append(requestType)
                    .append("&format=").append(format);
            if (stationString != null) {
                stringBuilder.append("&stationString=").append(stationString);
            }
            if (radialDistance != null) {
                stringBuilder.append("&radialDistance=").append(radialDistance);
            }
            if (hoursBeforeNow != 0) {
                stringBuilder.append("&hoursBeforeNow=").append(hoursBeforeNow);
            }
            stringBuilder.append("&mostRecentForEachStation=").append(mostRecentForEachStation);
            return stringBuilder.toString();
        }

        public String getDataSource() {
            return dataSource;
        }

        public MetarUriBuilder setDataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public String getRequestType() {
            return requestType;
        }

        public MetarUriBuilder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public String getFormat() {
            return format;
        }

        public MetarUriBuilder setFormat(String format) {
            this.format = format;
            return this;
        }

        public String getStationString() {
            return stationString;
        }

        public MetarUriBuilder setStationString(String stationString) {
            this.stationString = stationString;
            return this;
        }

        public String getRadialDistance() {
            return radialDistance;
        }

        public MetarUriBuilder setRadialDistance(int distanceMiles, double lat, double lon) {
            this.radialDistance = distanceMiles + ";" + lon + "," + lat;
            return this;
        }

        public int getHoursBeforeNow() {
            return hoursBeforeNow;
        }

        public MetarUriBuilder setHoursBeforeNow(int hoursBeforeNow) {
            this.hoursBeforeNow = hoursBeforeNow;
            return this;
        }

        public boolean isMostRecentForEachStation() {
            return mostRecentForEachStation;
        }

        public MetarUriBuilder setMostRecentForEachStation(boolean mostRecentForEachStation) {
            this.mostRecentForEachStation = mostRecentForEachStation;
            return this;
        }
    }

    private static class AviationweathergovMetar extends Metar {
        // TODO: This is not right, but I can't get the damn parser to take 'Z' as a time zone.
        private static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        public static AviationweathergovMetar createFromDomNode(Node node) throws ParseException {
            AviationweathergovMetar metar = new AviationweathergovMetar();
            Station station = new Station();
            Location location = new Location("aviationweather.gov");
            station.setLocation(location);
            metar.setStation(station);

            for (Node item = node.getFirstChild(); item != null; item = item.getNextSibling()) {
                String textContent = item.getTextContent();
                switch (item.getNodeName()) {
                    case "raw_text":
                        metar.setText(textContent);
                        break;
                    case "station_id":
                        station.setIcaoCode(textContent);
                        break;
                    case "observation_time":
                        metar.setTime(timeFormat.parse(textContent));
                        break;
                    case "latitude":
                        location.setLatitude(Double.parseDouble(textContent));
                        break;
                    case "longitude":
                        location.setLongitude(Double.parseDouble(textContent));
                        break;
                    case "temp_c":
                        metar.setTemp(Double.parseDouble(textContent));
                        break;
                    case "dewpoint_c":
                        metar.setDewPoint(Double.parseDouble(textContent));
                        break;
                    case "wind_dir_degrees":
                        metar.setWindDirection(Integer.parseInt(textContent));
                        break;
                    case "wind_speed_kt":
                        metar.setWindSpeed(Integer.parseInt(textContent));
                        break;
                    case "wind_gust_kt":
                        metar.setWindGust(Integer.parseInt(textContent));
                        break;
                    case "visibility_statute_mi":
                        metar.setVisibility(Double.parseDouble(textContent));
                        break;
                    case "altim_in_hg":
                        metar.setQnh(Double.parseDouble(textContent) * 33.86389);
                        break;
                    case "sky_condition":
                        Element element = (Element)item;
                        CloudLayer.CloudCoverType cover = CloudLayer.CloudCoverType.fromString(
                                element.getAttribute("sky_cover"));
                        String cloud_base_str = element.getAttribute("cloud_base_ft_agl");
                        if (cloud_base_str != null) {
                            metar.getCloudLayers().add(new CloudLayer(cover));
                        } else {
                            metar.getCloudLayers().add(new CloudLayer(cover, Integer.parseInt(cloud_base_str)));
                        }
                        break;
                    case "elevation_m":
                        location.setAltitude(Double.parseDouble(textContent));
                        break;
                    default:
                        Log.e("METAR", "Unrecognized METAR field: " + item.getNodeName() +
                        ": " + textContent);
                }
            }

            return metar;
        }
    }

    private List<Metar> doQuery(String query) {
        Log.i("METAR", "Sending HTTP request: " + query);
        HttpGet getRequest = new HttpGet(query);
        HttpResponse response = null;
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("PilotPet");
        try {
            response = httpClient.execute(getRequest);
        } catch (IOException e) {
            Log.e("METAR", "Failed to fetch METARs", e);
            return null;
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Log.e("METAR", "HTTP request failed with status code " +
                    response.getStatusLine().getStatusCode() +
                    ", reason: " + response.getStatusLine().getReasonPhrase());
            return null;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document xml = documentBuilder.parse(response.getEntity().getContent());
            NodeList raw_text_nodes = xml.getElementsByTagName("METAR");
            List<Metar> result = new ArrayList<>();
            for (int i = 0; i < raw_text_nodes.getLength(); i++) {
                result.add(AviationweathergovMetar.createFromDomNode(raw_text_nodes.item(i)));
            }
            return result;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("METAR", "Failed to fetch HTTP response contents.", e);
        } catch (SAXException e) {
            Log.e("METAR", "Failed to parse response XML.", e);
        } catch (ParseException e) {
            Log.e("METAR", "Unable to parse date", e);
        } finally {
            httpClient.close();
        }
        return null;
    }

    @Override
    public Metar getLatestForStation(String icaoCode) {
        String query = new MetarUriBuilder()
                .setStationString(icaoCode)
                .setMostRecentForEachStation(true)
                .build();
        return doQuery(query).get(0);
    }

    @Override
    public List<Metar> getLatestForClosestStations(int distanceMiles, Location currentLocation) {
        String metarUri = new MetarUriBuilder()
                .setRadialDistance(
                        distanceMiles, currentLocation.getLatitude(),
                        currentLocation.getLongitude())
                .setMostRecentForEachStation(true)
                .build();
        return doQuery(metarUri);
    }
}
