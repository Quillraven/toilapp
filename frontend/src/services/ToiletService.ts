import {API_ENDPOINT} from "./ServiceConstants";
import axios from "axios";
import {GeoLocation} from "../model/GeoLocation";
import {ToiletOverview} from "../model/ToiletOverview";
import {ToiletDetails} from "../model/ToiletDetails";

export interface ToiletService {
    getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]>

    getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails>
}

export class RestToiletService implements ToiletService {
    public getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]> {
        return axios
            .get(
                API_ENDPOINT + `/v1/toilets?` +
                `lon=${geoLocation.lon}` +
                `&lat=${geoLocation.lat}` +
                `&maxDistanceInMeters=${maxDistanceInMeters}`
            )
            .then(response => {
                const toiletOverviews: ToiletOverview[] = response.data

                toiletOverviews.filter(it => it.previewURL)
                    .forEach(it => it.previewURL = API_ENDPOINT + it.previewURL)

                return response.data
            }, error => {
                console.error(`Could not load toilets. Error=${error}`)
            })
    }

    public getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails> {
        console.log(`getToiletDetails for '${toiletId}'`)

        return axios
            .get(
                API_ENDPOINT + `/v1/toilets/${toiletId}?` +
                `lon=${location.lon}` +
                `&lat=${location.lat}`
            )
            .then(response => {
                const toiletDetails: ToiletDetails = response.data

                if (toiletDetails.previewURL) {
                    toiletDetails.previewURL = API_ENDPOINT + toiletDetails.previewURL
                }

                return response.data
            }, error => {
                console.error(`Could not load toilet details for '${toiletId}'. Error=${error}`)
            })
    }
}

export class MockToiletService implements ToiletService {
    public getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]> {
        return new Promise<ToiletOverview[]>(function (resolve) {
            const toilets: ToiletOverview[] = [
                {
                    id: "1",
                    title: "Beautiful toilet",
                    distance: 2313.0,
                    previewURL: "/toilet.jpg",
                    rating: 4.6,
                    disabled: false,
                    toiletCrewApproved: true,
                },
                {
                    id: "2",
                    title: "Dirty toilet",
                    distance: 893.0,
                    previewURL: "/toilet2.jpg",
                    rating: 1.3,
                    disabled: false,
                    toiletCrewApproved: true,
                },
                {
                    id: "3",
                    title: "Porta Potty",
                    distance: 7384.2,
                    previewURL: "/toilet.jpg",
                    rating: 2.5,
                    disabled: false,
                    toiletCrewApproved: true,
                },
                {
                    id: "4",
                    title: "Shit Heaven",
                    distance: 341.12,
                    previewURL: "/toilet2.jpg",
                    rating: 2.5,
                    disabled: false,
                    toiletCrewApproved: true,
                },
            ];
            resolve(toilets);
        });
    }

    getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails> {
        return new Promise<ToiletDetails>(function (resolve) {
            const toiletDetails: ToiletDetails = {
                id: "1",
                title: "Beautiful toilet",
                description: "Best shit experience you'll ever have!",
                location: {
                    lat: 42,
                    lon: 42,
                },
                distance: 2313.0,
                previewURL: "/toilet.jpg",
                rating: 4.6,
                numComments: 0,
                disabled: false,
                toiletCrewApproved: true,
            }
            resolve(toiletDetails)
        })
    }
}
