import { API_ENDPOINT } from "./ServiceConstants";
import axios, { AxiosResponse } from "axios";

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
    title: string
    location: GeoPoint
    preview: string
    rating: number
    disable: boolean
    toiletCrewApproved: boolean
    description: string
    comments: Array<Comment>
    images: Array<string>
}

export interface ToiletService {
    getToilets(): Promise<Toilet[]>; 
}

export class RestToiletService implements ToiletService  {
   
    public getToilets(): Promise<Toilet[]> {
        return this.mapData<Toilet[]>(axios.request<Toilet[]>({url: API_ENDPOINT + '/toilets'}), resp => resp.data);
    }

    private mapData<T>(promise: Promise<AxiosResponse<T>>, map: { (resp: AxiosResponse<T>): T } ): Promise<T> {
        return new Promise(function(resolve, reject) {
            promise.then(resp => resolve(map(resp)));
            promise.catch(e => resolve(e));
        });
    }
}

export class MockToiletService implements ToiletService {
    getToilets(): Promise<Toilet[]> {
       return new Promise<Toilet[]>(function(resolve, reject) {
            const toilets: Toilet[] = [
                {
                    title: "Beautiful toilet",
                    location: {x: 47.0, y: 16.0},
                    preview: "/toilet.jpg",
                    rating: 4.6,
                    disable: false,
                    toiletCrewApproved: true,
                    description: "Very very great",
                    comments: [],
                    images: []
                },
                {
                    title: "Dirty toilet",
                    location: {x: 47.0, y: 16.0},
                    preview: "/toilet2.jpg",
                    rating: 1.3,
                    disable: false,
                    toiletCrewApproved: true,
                    description: "Disgusting",
                    comments: [],
                    images: []
                },
                {
                    title: "Porta Potty",
                    location: {x: 47.0, y: 16.0},
                    preview: "/toilet.jpg",
                    rating: 2.5,
                    disable: false,
                    toiletCrewApproved: true,
                    description: "Smells very good ;)",
                    comments: [],
                    images: []
                },
                {
                    title: "Shit Heaven",
                    location: {x: 47.0, y: 16.0},
                    preview: "/toilet2.jpg",
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