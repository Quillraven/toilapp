import axios from "axios";
import {Rating} from "../model/Rating";
import {CreateUpdateRating} from "../model/CreateUpdateRating";

export class RatingServiceProvider {
    private static instance: RatingService

    private constructor() {
    }

    public static getRatingService(): RatingService {
        if (!RatingServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE === "true") {
                RatingServiceProvider.instance = new MockRatingService()
            } else {
                RatingServiceProvider.instance = new RestRatingService()
            }
        }

        return RatingServiceProvider.instance
    }
}

export interface RatingService {
    getUserRating(toiletId: string): Promise<Rating>

    createUpdateRating(toiletId: string, rating: number, ratingId?: string): Promise<Rating>
}

class RestRatingService implements RatingService {
    public getUserRating(toiletId: string): Promise<Rating> {
        console.log(`getUserRating for toilet '${toiletId}'`)

        return axios
            .get(process.env.REACT_APP_API_ENDPOINT + `/v1/ratings?toiletId=${toiletId}`)
            .then(response => {
                return response.data
            }, error => {
                console.error(`Could not get rating for toilet '${toiletId}'. Error=${error}`)
            })
    }

    public createUpdateRating(toiletId: string, rating: number, ratingId?: string): Promise<Rating> {
        console.log(`createUpdateRating for toilet '${toiletId}': (rating='${rating}', ratingId='${ratingId}')`)

        if (ratingId) {
            return axios
                .put(
                    process.env.REACT_APP_API_ENDPOINT + `/v1/ratings`,
                    {
                        ratingId: ratingId,
                        toiletId: toiletId,
                        value: rating,
                    } as CreateUpdateRating
                )
                .then(response => {
                    return response.data
                }, error => {
                    console.error(`Could not update rating '${ratingId}' for toilet '${toiletId}'. Error=${error}`)
                })
        } else {
            return axios
                .post(
                    process.env.REACT_APP_API_ENDPOINT + `/v1/ratings`,
                    {
                        ratingId: ratingId,
                        toiletId: toiletId,
                        value: rating,
                    } as CreateUpdateRating
                )
                .then(response => {
                    return response.data
                }, error => {
                    console.error(`Could not create rating for toilet '${toiletId}'. Error=${error}`)
                })
        }
    }
}

class MockRatingService implements RatingService {
    getUserRating(toiletId: string): Promise<Rating> {
        return new Promise<Rating>(function (resolve) {
            resolve({
                id: "",
                user: {
                    id: "",
                    email: "",
                    name: ""
                },
                value: 0,
            })
        });
    }

    createUpdateRating(toiletId: string, rating: number, ratingId?: string): Promise<Rating> {
        return new Promise<Rating>(function (resolve) {
            resolve({
                id: ratingId ? ratingId : "",
                user: {
                    id: "userId",
                    email: "test@gmail.com",
                    name: "mock-comment-user"
                },
                value: rating,
            })
        });
    }
}
