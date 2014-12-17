package nu.cmd.pilotpet.models;

import java.util.HashMap;

/**
* Created by aa on 12/14/2014.
*/
public class CloudLayer {
    /**
     * Source: http://en.wikipedia.org/wiki/METAR#Cloud_reporting
     */
    public enum CloudCoverType {
        OTHER,
        CAVOK,
        CLR("SKC"),
        NSC,
        FEW,
        SCT,
        BKN,
        OVC,
        VV("OVX");

        static final HashMap<String, CloudCoverType> aliasMap = new HashMap<>();
        static {
            for (CloudCoverType coverType : CloudCoverType.values()) {
                aliasMap.put(coverType.name(), coverType);
                for (String name : coverType.aliases) {
                    aliasMap.put(name, coverType);
                }
            }
        }

        private final String[] aliases;
        private CloudCoverType(String... aliases) {
            this.aliases = aliases;
        }

        public static CloudCoverType fromString(String str) {
            try {
                return aliasMap.get(str);
            } catch (IllegalArgumentException e) {
                return OTHER;
            }
        }
    }

    private CloudCoverType cover;
    private int altitude;

    public CloudLayer(CloudCoverType cover, int altitude) {
        this.cover = cover;
        this.altitude = altitude;
    }

    public CloudLayer(CloudCoverType cover) {
        this.cover = cover;
        this.altitude = -1;
    }

    @Override
    public String toString() {
        return "CloudLayer{" +
                "cover=" + cover +
                ", altitude=" + altitude +
                '}';
    }
}
