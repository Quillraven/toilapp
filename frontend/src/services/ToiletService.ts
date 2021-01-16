import axios from "axios";
import {GeoLocation} from "../model/GeoLocation";
import {ToiletOverview} from "../model/ToiletOverview";
import {ToiletDetails} from "../model/ToiletDetails";
import {CreateUpdateToilet} from "../model/CreateUpdateToilet";
import {errorPromise} from "./ServiceUtils";

export abstract class ToiletServiceProvider {
    private static instance: ToiletService

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

export function getDistanceString(distanceInKm: number) {
    if (distanceInKm >= 1000) {
        return "> 1000km";
    } else if (distanceInKm < 0) {
        return "-";
    } else {
        return distanceInKm.toFixed(2) + "km";
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

    getToilets(location: GeoLocation, maxDistanceInKm: number, maxToiletsToLoad: number, minDistance: number, idsToExclude: Array<String>): Promise<ToiletOverview[]>

    getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails>
}

class RestToiletService implements ToiletService {
    async createUpdateToilet(
        title: string,
        location: GeoLocation,
        disabled: boolean,
        description?: string,
        toiletId?: string
    ): Promise<ToiletDetails> {
        try {
            const response = await axios(
                {
                    method: toiletId ? "PUT" : "POST",
                    url: `/v1/toilets`,
                    data: {
                        id: toiletId,
                        title: title,
                        description: description,
                        location: {
                            x: location.x,
                            y: location.y
                        },
                        disabled: disabled,
                        toiletCrewApproved: false,
                    } as CreateUpdateToilet,
                }
            )
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during createUpdateToilet")
        }
    }

    async updatePreviewImage(toiletId: string, image: File): Promise<string> {
        try {
            const formData = new FormData()
            formData.append("file", image)
            const response = await axios.put(
                `/v1/images/preview?toiletId=${toiletId}`,
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data"
                    }
                }
            )
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during updatePreviewImage")
        }
    }

    async getToilets(
        location: GeoLocation,
        maxDistanceInKm: number,
        maxToiletsToLoad: number,
        minDistance: number = 0,
        idsToExclude: Array<String> = []
    ): Promise<ToiletOverview[]> {
        try {
            const response = await axios.get(
                `/v1/toilets`,
                {
                    params: {
                        lon: location.x,
                        lat: location.y,
                        radiusInKm: maxDistanceInKm,
                        maxToiletsToLoad: maxToiletsToLoad,
                        minDistanceInKm: minDistance,
                        toiletIdsToExclude: idsToExclude.join(",")
                    }
                }
            )
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during getToilets")
        }
    }

    async getToiletDetails(toiletId: string, location: GeoLocation): Promise<ToiletDetails> {
        console.log(`getToiletDetails for '${toiletId}'`)
        try {
            const response = await axios.get(`/v1/toilets/${toiletId}?lon=${location.x}&lat=${location.y}`)
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during getToiletDetails")
        }
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

    public getToilets(
        location: GeoLocation,
        maxDistanceInKm: number,
        maxToiletsToLoad: number,
        minDistance: number = 0,
        idsToExclude: Array<String> = []
    ): Promise<ToiletOverview[]> {
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
                    y: 42,
                    x: 42,
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
