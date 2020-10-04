import {API_ENDPOINT} from "./ServiceConstants";
import axios from "axios";
import { GeoLocation } from "./GeoLocationService";
import ToiletOverview from "../components/ToiletOverview";

export interface User {
    id: string
    name: string
    email: string
}

export interface Comment {
    user: User
    date: Date
    text: string
}

export interface GeoPoint {
    x: number
    y: number
}

export interface Toilet {
    id: string
    title: string
    location: GeoPoint
    previewID: string
    previewImage: any
    rating: number
    disable: boolean
    toiletCrewApproved: boolean
    description: string
    comments: Array<Comment>
    images: Array<string>
}

export interface ToiletResult {
    toilet: Toilet,
    distance: number
}

export interface ToiletService {
    getToilets(geoLocation: GeoLocation, loadImages?: boolean): Promise<ToiletResult[]>;
}

export class RestToiletService implements ToiletService {
    private maxDistance = 2000000;
    
    public getToilets(geoLocation: GeoLocation, loadImages: boolean = true): Promise<ToiletResult[]> {
        return axios
            .get(API_ENDPOINT + '/toilets?lon=' + geoLocation.lon + "&lat=" + geoLocation.lat + "&maxDistanceInMeters=" + this.maxDistance)
            .then(response => {
                if (loadImages) {
                    const toilets: ToiletResult[] = response.data
                    toilets
                        .filter(it => it.toilet.previewID)
                        .forEach(toilet => toilet.toilet.previewImage = API_ENDPOINT + `/previews/${toilet.toilet.previewID}`)
                }
                return response.data
            }, error => {
                console.error(`Could not load toilets. Error=${error}`)
            })
    }
}

export class MockToiletService implements ToiletService {
    getToilets(): Promise<ToiletResult[]> {
        return new Promise<ToiletResult[]>(function (resolve, reject) {
            const toilets: ToiletResult[] = [
                { 
                    toilet: {
                        id: "1",
                        title: "Beautiful toilet",
                        location: {x: 47.0, y: 16.0},
                        previewID: "/toilet.jpg",
                        previewImage: "",
                        rating: 4.6,
                        disable: false,
                        toiletCrewApproved: true,
                        description: "Very very great",
                        comments: [],
                        images: []
                    },
                    distance: 2313.0
                },
                {                 
                    toilet: {
                        id: "2",
                        title: "Dirty toilet",
                        location: {x: 47.0, y: 16.0},
                        previewID: "/toilet2.jpg",
                        previewImage: "",
                        rating: 1.3,
                        disable: false,
                        toiletCrewApproved: true,
                        description: "Disgusting",
                        comments: [],
                        images: []
                    },
                    distance: 893.0
                },
                {
                    toilet: {
                        id: "3",
                        title: "Porta Potty",
                        location: {x: 47.0, y: 16.0},
                        previewID: "/toilet.jpg",
                        previewImage: "",
                        rating: 2.5,
                        disable: false,
                        toiletCrewApproved: true,
                        description: "Smells very good ;)",
                        comments: [],
                        images: []
                    },
                    distance: 7384.2
                },
                {
                    toilet: {
                        id: "4",
                        title: "Shit Heaven",
                        location: {x: 47.0, y: 16.0},
                        previewID: "/toilet2.jpg",
                        previewImage: "",
                        rating: 2.5,
                        disable: false,
                        toiletCrewApproved: true,
                        description: "So wonderful!!!!",
                        comments: [],
                        images: []
                    },
                    distance: 341.12
                },
            ];
            resolve(toilets);
        });
    }
}
