import {API_ENDPOINT} from "./ServiceConstants";
import axios from "axios";
import {Toilet} from "../model/Toilet";
import {GeoLocation} from "../model/GeoLocation";
import {ToiletComment} from "../model/ToiletComment";

export interface ToiletService {
    getToilets(geoLocation: GeoLocation): Promise<Toilet[]>

    getComments(toilet: Toilet): Promise<ToiletComment[]>

    postComment(toiletId: string, userId: string, text: string): Promise<ToiletComment>
}

export class RestToiletService implements ToiletService {
    private maxDistance = 2000000;

    public getToilets(geoLocation: GeoLocation): Promise<Toilet[]> {
        return axios
            .get(API_ENDPOINT + '/toilets?lon=' + geoLocation.lon + "&lat=" + geoLocation.lat + "&maxDistanceInMeters=" + this.maxDistance)
            .then(response => {
                const toilets: Toilet[] = response.data

                toilets.filter(it => it.previewURL)
                    .forEach(it => it.previewURL = API_ENDPOINT + it.previewURL)

                return response.data
            }, error => {
                console.error(`Could not load toilets. Error=${error}`)
            })
    }

    public getComments(toilet: Toilet): Promise<ToiletComment[]> {
        return axios
            .get(API_ENDPOINT + `/comments/${toilet.id}`)
            .then(response => {
                const comments: ToiletComment[] = response.data

                // TODO is there a way to retrieve already a correct date instance from REST directly?
                //  we currently receive a string
                comments.forEach(it => it.date = new Date(it.date))

                return response.data
            }, error => {
                console.error(`Could not get comments for toilet '${toilet.id}'. Error=${error}`)
            })
    }

    public postComment(toiletId: string, userId: string, text: string): Promise<ToiletComment> {
        return axios
            .post(API_ENDPOINT + `/comments/?toiletId=${toiletId}&userId=${userId}&text=${encodeURI(text)}`)
            .then(response => {
                const comment: ToiletComment = response.data

                // TODO is there a way to retrieve already a correct date instance from REST directly?
                //  we currently receive a string
                comment.date = new Date(comment.date)

                return response.data
            }, error => {
                console.error(`Could not post comment for toilet '${toiletId}'. Error=${error}`)
            })
    }
}

export class MockToiletService implements ToiletService {
    getToilets(): Promise<Toilet[]> {
        return new Promise<Toilet[]>(function (resolve) {
            const toilets: Toilet[] = [
                {
                    id: "1",
                    title: "Beautiful toilet",
                    location: {lon: 47.0, lat: 16.0},
                    previewURL: "/toilet.jpg",
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
                    previewURL: "/toilet2.jpg",
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
                    previewURL: "/toilet.jpg",
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
                    previewURL: "/toilet2.jpg",
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

    getComments(toilet: Toilet): Promise<ToiletComment[]> {
        return new Promise<ToiletComment[]>(function (resolve) {
            const comments: ToiletComment[] = []
            resolve(comments)
        })
    }

    postComment(toiletId: string, userId: string, text: string): Promise<ToiletComment> {
        return new Promise<ToiletComment>(function (resolve) {
            const comment: ToiletComment = {
                id: "",
                user: {
                    name: "",
                    email: "",
                    id: ""
                },
                date: new Date(),
                text: ""
            }
            resolve(comment)
        })
    }
}
