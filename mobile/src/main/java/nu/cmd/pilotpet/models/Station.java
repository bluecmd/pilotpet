package nu.cmd.pilotpet.models;

import android.location.Location;

/**
 * Created by aa on 12/14/2014.
 */
public class Station {
    private String longName;
    private String icaoCode;
    private Location location;

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getIcaoCode() {
        return icaoCode;
    }

    public void setIcaoCode(String icaoCode) {
        this.icaoCode = icaoCode;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Station{");
        sb.append("longName='").append(longName).append('\'');
        sb.append(", icaoCode='").append(icaoCode).append('\'');
        sb.append(", location=").append(location);
        sb.append('}');
        return sb.toString();
    }
}
