import {API_ENDPOINT} from "./ServiceConstants";
import axios from "axios";

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

export interface ToiletService {
    getToilets(loadImages?: boolean): Promise<Toilet[]>;
}

export class RestToiletService implements ToiletService {
    public getToilets(loadImages: boolean = true): Promise<Toilet[]> {
        return axios
            .get(API_ENDPOINT + '/toilets')
            .then(response => {
                if (loadImages) {
                    const toilets: Toilet[] = response.data
                    toilets
                        .filter(it => it.previewID)
                        .forEach(toilet => toilet.previewImage = API_ENDPOINT + `/previews/${toilet.previewID}`)
                }
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
                {
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
                {
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
                {
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
                }
            ];
            resolve(toilets);
        });
    }
}
