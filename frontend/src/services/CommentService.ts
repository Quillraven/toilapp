import axios from "axios";
import {ToiletComment} from "../model/ToiletComment";
import {CreateUpdateComment} from "../model/CreateUpdateComment";

export interface CommentService {
    getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]>

    postComment(toiletId: string, text: string): Promise<ToiletComment>
}

export class RestCommentService implements CommentService {
    public getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]> {
        console.log(`getComments: (toiletId=${toiletId}, page=${page}, numComments=${numComments}`)

        return axios
            .get(
                process.env.REACT_APP_API_ENDPOINT + `/v1/comments/${toiletId}?` +
                `page=${page}` +
                `&numComments=${numComments}`
            )
            .then(response => {
                return response.data
            }, error => {
                console.error(`Could not get comments for toilet '${toiletId}'. Error=${error}`)
            })
    }

    public postComment(toiletId: string, text: string): Promise<ToiletComment> {
        console.log(`Posting '${text}'`)

        return axios
            .post(
                process.env.REACT_APP_API_ENDPOINT + `/v1/comments`,
                {
                    commentId: "",
                    toiletId: toiletId,
                    text: text,
                } as CreateUpdateComment
            )
            .then(response => {
                return response.data
            }, error => {
                console.error(`Could not post comment for toilet '${toiletId}'. Error=${error}`)
            })
    }
}
