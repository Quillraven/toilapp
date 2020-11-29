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

export const EMPTY_DETAILS: ToiletDetails = {
    id: "",
    title: "",
    description: "",
    location: {
        lon: 0,
        lat: 0
    },
    distance: 0,
    previewURL: "",
    rating: 0,
    numComments: 0,
    disabled: false,
    toiletCrewApproved: false,
}
