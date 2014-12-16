package nu.cmd.pilotpet.dao;

import android.location.Location;

import java.util.List;

import nu.cmd.pilotpet.models.Metar;

/**
 * Created by aa on 12/14/2014.
 */
public interface MetarDao {
    Metar getLatestForStation(String icaoCode);
    List<Metar> getLatestForClosestStations(int distanceMiles, Location currentLocation);
}
