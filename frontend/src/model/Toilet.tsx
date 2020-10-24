import {ToiletComment} from "./ToiletComment";
import {GeoLocation} from "./GeoLocation";

export interface Toilet {
    id: string,
    title: string,
    location: GeoLocation,
    distance: number,
    previewURL: string,
    rating: number,
    disabled: boolean,
    toiletCrewApproved: boolean,
    description: string,
    comments: Array<ToiletComment>,
    images: Array<string>,
}
