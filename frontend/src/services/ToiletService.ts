import {API_ENDPOINT} from "./ServiceConstants";
import axios from "axios";
import {Toilet} from "../model/Toilet";
import {GeoLocation} from "../model/GeoLocation";

export interface ToiletService {
    getToilets(geoLocation: GeoLocation, loadImages?: boolean): Promise<Toilet[]>;
}

export class RestToiletService implements ToiletService {
    private maxDistance = 2000000;

    public getToilets(geoLocation: GeoLocation): Promise<Toilet[]> {
        return axios
            .get(API_ENDPOINT + '/toilets?lon=' + geoLocation.lon + "&lat=" + geoLocation.lat + "&maxDistanceInMeters=" + this.maxDistance)
            .then(response => {
                return response.data
            }, error => {
                console.error(`Could not load toilets. Error=${error}`)
            })
    }
}

export class MockToiletService implements ToiletService {
    getToilets(): Promise<Toilet[]> {
        return new Promise<Toilet[]>(function (resolve, reject) {
            const toilets: Toilet[] = [
                {
                    id: "1",
                    title: "Beautiful toilet",
                    location: {lon: 47.0, lat: 16.0},
                    previewID: "/toilet.jpg",
                    rating: 4.6,
                    disabled: false,
                    toiletCrewApproved: true,
                    description: "Very very great",
                    comments: [],
                    images: [],
                    distance: 2313.0
                },
                {
                    id: "2",
                    title: "Dirty toilet",
                    location: {lon: 47.0, lat: 16.0},
                    previewID: "/toilet2.jpg",
                    rating: 1.3,
                    disabled: false,
                    toiletCrewApproved: true,
                    description: "Disgusting",
                    comments: [],
                    images: [],
                    distance: 893.0
                },
                {
                    id: "3",
                    title: "Porta Potty",
                    location: {lon: 47.0, lat: 16.0},
                    previewID: "/toilet.jpg",
                    rating: 2.5,
                    disabled: false,
                    toiletCrewApproved: true,
                    description: "Smells very good ;)",
                    comments: [],
                    images: [],
                    distance: 7384.2
                },
                {
                    id: "4",
                    title: "Shit Heaven",
                    location: {lon: 47.0, lat: 16.0},
                    previewID: "/toilet2.jpg",
                    rating: 2.5,
                    disabled: false,
                    toiletCrewApproved: true,
                    description: "So wonderful!!!!",
                    comments: [],
                    images: [],
                    distance: 341.12
                },
            ];
            resolve(toilets);
        });
    }
}
