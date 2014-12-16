package nu.cmd.pilotpet.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by aa on 12/14/2014.
 */
public class Metar {

    private String text;
    private Date time;
    private Station station;
    private double qnh;
    private double temp;
    private double dewPoint;
    private int windDirection;
    private int windSpeed;
    private double visibility;
    private List<CloudLayer> cloudLayers = new ArrayList<>();
    private int windGust;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public double getQnh() {
        return qnh;
    }

    public void setQnh(double qnh) {
        this.qnh = qnh;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindGust(int windGust) {
        this.windGust = windGust;
    }

    public int getWindGust() {
        return windGust;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public List<CloudLayer> getCloudLayers() {
        return cloudLayers;
    }

    public void setCloudLayers(List<CloudLayer> cloudLayers) {
        this.cloudLayers = cloudLayers;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Metar{");
        sb.append("text='").append(text).append('\'');
        sb.append(", time=").append(time);
        sb.append(", station=").append(station);
        sb.append(", qnh=").append(qnh);
        sb.append(", temp=").append(temp);
        sb.append(", dewPoint=").append(dewPoint);
        sb.append(", windDirection=").append(windDirection);
        sb.append(", windSpeed=").append(windSpeed);
        sb.append(", windGust=").append(windGust);
        sb.append(", visibility=").append(visibility);
        sb.append(", cloudLayers=").append(Arrays.deepToString(cloudLayers.toArray()));
        sb.append('}');
        return sb.toString();
    }
}
