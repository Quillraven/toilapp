import {GeoLocation} from "../model/GeoLocation";

export interface GeoLocationService {
    getGeoLocation(): GeoLocation
}

export class DefaultGeoLocationService implements GeoLocationService {
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
