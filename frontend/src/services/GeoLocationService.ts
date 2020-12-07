import {GeoLocation} from "../model/GeoLocation";

export class GeoLocationServiceProvider {
    private static instance: GeoLocationService

    private constructor() {
    }

    public static getGeoLocationService(): GeoLocationService {
        console.debug("getGeoLocationService")
        if (!GeoLocationServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE) {
                console.debug("Creating new mock GeoLocationService")
                GeoLocationServiceProvider.instance = new MockGeoLocationService()
            } else {
                console.debug("Creating new default GeoLocationService")
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

        //If not no available: retrun default loaction
        const geoLoation = {
            lon: 16.0,
            lat: 47.0
        };
        return geoLoation;
    }
}

class MockGeoLocationService implements GeoLocationService {
    getGeoLocation = () => ({
        lon: 16.0,
        lat: 47.0
    })
}
