import {GeoLocation} from "./GeoLocation";

export interface ToiletDetails {
    id: string,
    title: string,
    description: string,
    location: GeoLocation,
    distance: number,
    previewURL: string,
    rating: number,
    numComments: number,
    disabled: boolean,
    toiletCrewApproved: boolean,
}
