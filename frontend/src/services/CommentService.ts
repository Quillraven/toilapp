import axios from "axios";
import {ToiletComment} from "../model/ToiletComment";
import {CreateUpdateComment} from "../model/CreateUpdateComment";

export class CommentServiceProvider {
    private static instance: CommentService

    private constructor() {
    }

    public static getCommentService(): CommentService {
        console.debug("getCommentService")
        if (!CommentServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE) {
                console.debug("Creating new mock CommentService")
                CommentServiceProvider.instance = new MockCommentService()
            } else {
                console.debug("Creating new rest CommentService")
                CommentServiceProvider.instance = new RestCommentService()
            }
        }

        return CommentServiceProvider.instance
    }
}

export interface CommentService {
    getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]>

    postComment(toiletId: string, text: string): Promise<ToiletComment>
}

class RestCommentService implements CommentService {
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

class MockCommentService implements CommentService {
    getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]> {
        return new Promise<ToiletComment[]>(function (resolve) {
            const comments: ToiletComment[] = [
                {
                    id: "1",
                    localDateTime: {
                        dayOfMonth: 12,
                        monthValue: 6,
                        year: 2020,
                        hour: 14,
                        minute: 30,
                        second: 0
                    },
                    text: "First comment",
                    user: {
                        id: "userId",
                        email: "test@gmail.com",
                        name: "mock-comment-user"
                    }
                },
                {
                    id: "2",
                    localDateTime: {
                        dayOfMonth: 12,
                        monthValue: 6,
                        year: 2020,
                        hour: 14,
                        minute: 40,
                        second: 0
                    },
                    text: "Second comment\n\nmulti-line",
                    user: {
                        id: "userId",
                        email: "test@gmail.com",
                        name: "mock-comment-user"
                    }
                },
                {
                    id: "3",
                    localDateTime: {
                        dayOfMonth: 13,
                        monthValue: 6,
                        year: 2020,
                        hour: 14,
                        minute: 40,
                        second: 0
                    },
                    text: "Third comment a day later",
                    user: {
                        id: "userId",
                        email: "test@gmail.com",
                        name: "mock-comment-user"
                    }
                },
            ];

            for (let i = 0; i < 42; i++) {
                comments.push(
                    {
                        id: `${(i + 4)}`,
                        localDateTime: {
                            dayOfMonth: 15,
                            monthValue: 7,
                            year: 2020,
                            hour: 14,
                            minute: i,
                            second: i
                        },
                        text: `Comment ${i + 4}`,
                        user: {
                            id: "userId",
                            email: "test@gmail.com",
                            name: "mock-comment-user"
                        }
                    },
                )
            }

            resolve(comments);
        });
    }

    postComment(toiletId: string, text: string): Promise<ToiletComment> {
        return new Promise<ToiletComment>(function (resolve) {
            resolve(
                {
                    id: "42",
                    localDateTime: {
                        dayOfMonth: 12,
                        monthValue: 6,
                        year: 2020,
                        hour: 14,
                        minute: 30,
                        second: 0
                    },
                    text: "A new comment",
                    user: {
                        id: "userId",
                        email: "test@gmail.com",
                        name: "mock-comment-user"
                    }
                } as ToiletComment
            );
        });
    }
}
