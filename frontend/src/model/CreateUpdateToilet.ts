import {GeoLocation} from "./GeoLocation";

export interface CreateUpdateToilet {
    id: string,
    title: string,
    description: string,
    location: GeoLocation,
    disabled: boolean,
    toiletCrewApproved: boolean,
}
