export interface GeoLocationParam {
    type: string,
    coordinates: number[]
}

export interface CreateUpdateToilet {
    id: string,
    title: string,
    description: string,
    location: GeoLocationParam,
    disabled: boolean,
    toiletCrewApproved: boolean,
}
