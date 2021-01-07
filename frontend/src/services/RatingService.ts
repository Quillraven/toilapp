import axios from "axios";
import {Rating} from "../model/Rating";
import {CreateUpdateRating} from "../model/CreateUpdateRating";
import {errorPromise} from "./ServiceUtils";

export abstract class RatingServiceProvider {
    private static instance: RatingService

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
    async getUserRating(toiletId: string): Promise<Rating> {
        console.log(`getUserRating for toilet '${toiletId}'`)

        try {
            const response = await axios.get(`/v1/ratings?toiletId=${toiletId}`)
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during getUserRating")
        }
    }

    async createUpdateRating(toiletId: string, rating: number, ratingId?: string): Promise<Rating> {
        console.log(`createUpdateRating for toilet '${toiletId}': (rating='${rating}', ratingId='${ratingId}')`)

        try {
            const response = await axios(
                {
                    method: ratingId ? "PUT" : "POST",
                    url: `/v1/ratings`,
                    data: {
                        ratingId: ratingId,
                        toiletId: toiletId,
                        value: rating,
                    } as CreateUpdateRating,
                }
            )
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during createUpdateRating")
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
