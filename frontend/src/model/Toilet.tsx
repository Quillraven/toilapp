import {Comment} from "./Comment";
import {GeoLocation} from "./GeoLocation";

export interface Toilet {
    id: string,
    title: string,
    location: GeoLocation,
    distance: number,
    previewID: string,
    rating: number,
    disabled: boolean,
    toiletCrewApproved: boolean,
    description: string,
    comments: Array<Comment>,
    images: Array<string>,
}
