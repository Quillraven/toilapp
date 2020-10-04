export interface GeoLocation {
    lon: number,
    lat: number
}

export interface GeoLocationService {
    getGeoLocation(): GeoLocation
}

export class DefaultGeoLocationService implements GeoLocationService {
    getGeoLocation(): GeoLocation {
        //on a mobile phone -> Ask the mobile OS for the location

        //If not available: on a computer -> Ask a IP to loaction web service for the location (e.g. Google.)

        //If not no available: retrun default loaction
        const geoLoation = {
            lon: 16.0,
            lat: 47.0
        };
        return geoLoation;
    }

}