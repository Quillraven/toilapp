import axios from "axios";
import {GeoLocation} from "../model/GeoLocation";
import {ToiletOverview} from "../model/ToiletOverview";
import {ToiletDetails} from "../model/ToiletDetails";
import {CreateUpdateToilet} from "../model/CreateUpdateToilet";

export class ToiletServiceProvider {
    private static instance: ToiletService

    private constructor() {
    }

    public static getToiletService(): ToiletService {
        if (!ToiletServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE === "true") {
                ToiletServiceProvider.instance = new MockToiletService()
            } else {
                ToiletServiceProvider.instance = new RestToiletService()
            }
        }

        return ToiletServiceProvider.instance
    }
}

export function getDistanceString(distance: number) {
    if (distance >= 1000) {
        return (distance / 1000).toFixed(1) + "km";
    } else if (distance < 0) {
        return "-";
    } else {
        return distance.toFixed(0) + "m";
    }
}

export interface ToiletService {
    createUpdateToilet(
        title: string,
        location: GeoLocation,
        disabled: boolean,
        description?: string,
        toiletId?: string,
    ): Promise<ToiletDetails>

    updatePreviewImage(toiletId: string, image: File): Promise<string>

    getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]>

    getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails>
}

class RestToiletService implements ToiletService {
    createUpdateToilet(
        title: string,
        location: GeoLocation,
        disabled: boolean,
        description?: string,
        toiletId?: string
    ): Promise<ToiletDetails> {
        const body = {
            id: toiletId,
            title: title,
            description: description,
            location: {
                type: "Point",
                coordinates: [
                    location.lon,
                    location.lat
                ],
            },
            disabled: disabled,
            toiletCrewApproved: false,
        } as CreateUpdateToilet

        if (toiletId) {
            return axios
                .put(process.env.REACT_APP_API_ENDPOINT + `/v1/toilets`, body)
                .then(response => {
                    const toiletDetails: ToiletDetails = response.data

                    if (toiletDetails.previewURL) {
                        toiletDetails.previewURL = process.env.REACT_APP_API_ENDPOINT + toiletDetails.previewURL
                    }

                    return response.data
                }, error => {
                    console.error(`Could not update toilet '${toiletId}'. Error=${error}`)
                })
        } else {
            return axios
                .post(process.env.REACT_APP_API_ENDPOINT + `/v1/toilets`, body)
                .then(response => {
                    const toiletDetails: ToiletDetails = response.data

                    if (toiletDetails.previewURL) {
                        toiletDetails.previewURL = process.env.REACT_APP_API_ENDPOINT + toiletDetails.previewURL
                    }

                    return response.data
                }, error => {
                    console.error(`Could not create new toilet. Error=${error}`)
                })
        }
    }

    updatePreviewImage(toiletId: string, image: File): Promise<string> {
        const formData = new FormData()
        formData.append("file", image)

        return axios
            .put(
                process.env.REACT_APP_API_ENDPOINT + `/v1/images/preview?toiletId=${toiletId}`,
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data"
                    }
                }
            )
            .then(response => {
                return response.data
            }, error => {
                console.error(`Could not update preview for toilet '${toiletId}'. Error=${error}`)
            })
    }

    public getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]> {
        return axios
            .get(
                process.env.REACT_APP_API_ENDPOINT + `/v1/toilets?` +
                `lon=${geoLocation.lon}` +
                `&lat=${geoLocation.lat}` +
                `&maxDistanceInMeters=${maxDistanceInMeters}`
            )
            .then(response => {
                const toiletOverviews: ToiletOverview[] = response.data

                toiletOverviews.filter(it => it.previewURL)
                    .forEach(it => it.previewURL = process.env.REACT_APP_API_ENDPOINT + it.previewURL)

                return response.data
            }, error => {
                console.error(`Could not load toilets. Error=${error}`)
            })
    }

    public getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails> {
        console.log(`getToiletDetails for '${toiletId}'`)

        return axios
            .get(
                process.env.REACT_APP_API_ENDPOINT + `/v1/toilets/${toiletId}?` +
                `lon=${location.lon}` +
                `&lat=${location.lat}`
            )
            .then(response => {
                const toiletDetails: ToiletDetails = response.data

                if (toiletDetails.previewURL) {
                    toiletDetails.previewURL = process.env.REACT_APP_API_ENDPOINT + toiletDetails.previewURL
                }

                return response.data
            }, error => {
                console.error(`Could not load toilet details for '${toiletId}'. Error=${error}`)
            })
    }
}

class MockToiletService implements ToiletService {
    createUpdateToilet(
        title: string,
        location: GeoLocation,
        disabled: boolean,
        description?: string,
        toiletId?: string
    ): Promise<ToiletDetails> {
        return new Promise<ToiletDetails>(function (resolve) {
            const toiletDetails: ToiletDetails = {
                id: toiletId ? toiletId : "42",
                title: title,
                description: description ? description : "",
                location: location,
                distance: 1337.0,
                previewURL: "/mock/toilet1.jpg",
                rating: 3,
                numComments: 0,
                disabled: disabled,
                toiletCrewApproved: false,
            }
            resolve(toiletDetails)
        })
    }

    updatePreviewImage(toiletId: string, image: File): Promise<string> {
        return new Promise<string>(function (resolve) {
            resolve("42")
        })
    }

    public getToilets(geoLocation: GeoLocation, maxDistanceInMeters: number): Promise<ToiletOverview[]> {
        return new Promise<ToiletOverview[]>(function (resolve) {
            const toilets: ToiletOverview[] = [
                {
                    id: "1",
                    title: "Toilet 10m",
                    distance: 10.0,
                    previewURL: "/mock/toilet1.jpg",
                    rating: 0,
                    disabled: false,
                    toiletCrewApproved: false,
                },
                {
                    id: "2",
                    title: "Toilet longer name 100.25m",
                    distance: 100.25,
                    previewURL: "/mock/toilet2.jpg",
                    rating: 1.25,
                    disabled: false,
                    toiletCrewApproved: true,
                },
                {
                    id: "3",
                    title: "Toilet extra long name 1.5km",
                    distance: 1500.5,
                    previewURL: "/mock/toilet3.jpg",
                    rating: 3.5,
                    disabled: true,
                    toiletCrewApproved: false,
                },
                {
                    id: "4",
                    title: "Toilet extremly long name that never seems to end 10km",
                    distance: 10000.75,
                    previewURL: "/mock/toilet4.jpg",
                    rating: 4.75,
                    disabled: true,
                    toiletCrewApproved: true,
                },
                {
                    id: "5",
                    title: "Toilet highest rating 100.25km",
                    distance: 100250.0,
                    previewURL: "/mock/toilet5.jpg",
                    rating: 5,
                    disabled: true,
                    toiletCrewApproved: true,
                },
                {
                    id: "6",
                    title: "Toilet without image",
                    distance: 0.0,
                    previewURL: "",
                    rating: 1,
                    disabled: true,
                    toiletCrewApproved: true,
                },
            ];
            resolve(toilets);
        });
    }

    getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails> {
        return new Promise<ToiletDetails>(function (resolve) {
            const toiletDetails: ToiletDetails = {
                id: "1",
                title: "Beautiful toilet",
                description: "Best shit experience you'll ever have!",
                location: {
                    lat: 42,
                    lon: 42,
                },
                distance: 2313.0,
                previewURL: "/mock/toilet1.jpg",
                rating: 4.6,
                numComments: 100,
                disabled: true,
                toiletCrewApproved: true,
            }
            resolve(toiletDetails)
        })
    }
}
