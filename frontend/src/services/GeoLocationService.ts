import {GeoLocation} from "../model/GeoLocation";

export abstract class GeoLocationServiceProvider {
    private static instance: GeoLocationService

    public static getGeoLocationService(): GeoLocationService {
        if (!GeoLocationServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE === "true") {
                GeoLocationServiceProvider.instance = new MockGeoLocationService()
            } else {
                GeoLocationServiceProvider.instance = new DefaultGeoLocationService()
            }
        }

        return GeoLocationServiceProvider.instance
    }
}

export interface GeoLocationService {
    getGeoLocation(): GeoLocation
}

class DefaultGeoLocationService implements GeoLocationService {
    getGeoLocation(): GeoLocation {
        // TODO: on a mobile phone -> Ask the mobile OS for the location

        // TODO: If not available: on a computer -> Ask a IP to loaction web service for the location (e.g. Google.)

        return {
            x: 16.0,
            y: 47.0
        };
    }
}

class MockGeoLocationService implements GeoLocationService {
    getGeoLocation = () => ({
        x: 16.0,
        y: 47.0
    } as GeoLocation)
}
