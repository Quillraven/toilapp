import axios from "axios";
import {ToiletComment} from "../model/ToiletComment";
import {CreateUpdateComment} from "../model/CreateUpdateComment";
import {errorPromise} from "./ServiceUtils";

export abstract class CommentServiceProvider {
    private static instance: CommentService

    public static getCommentService(): CommentService {
        if (!CommentServiceProvider.instance) {
            if (process.env.REACT_APP_DEV_MODE === "true") {
                CommentServiceProvider.instance = new MockCommentService()
            } else {
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
    async getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]> {
        console.log(`getComments: (toiletId=${toiletId}, page=${page}, numComments=${numComments}`)

        try {
            const response = await axios.get(`/v1/comments/${toiletId}?page=${page}&numComments=${numComments}`)
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during getComments")
        }
    }

    async postComment(toiletId: string, text: string): Promise<ToiletComment> {
        console.log(`Posting '${text}'`)

        try {
            const response = await axios.post(
                `/v1/comments`,
                {
                    commentId: "",
                    toiletId: toiletId,
                    text: text,
                } as CreateUpdateComment
            )
            return Promise.resolve(response.data)
        } catch (error) {
            return errorPromise(error, "Error during postComment")
        }
    }
}

class MockCommentService implements CommentService {
    getComments(toiletId: string, page: number, numComments: number): Promise<ToiletComment[]> {
        return new Promise<ToiletComment[]>(function (resolve) {
            setTimeout(() => {
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

                const longCommentTxt = `Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.   \n\nDuis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.   \n\nUt wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.   \n\nNam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.   \n\nDuis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis.   \n\nAt vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, At accusam aliquyam diam diam dolore dolores duo eirmod eos erat, et nonumy sed tempor et et invidunt justo labore Stet clita ea et gubergren, kasd magna no rebum. sanctus sea sed takimata ut vero voluptua. est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur`

                for (let i = 0; i < numComments - 3; i++) {
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
                            text: `${(page * numComments) + i} ${longCommentTxt}`,
                            user: {
                                id: "userId",
                                email: "test@gmail.com",
                                name: "mock-comment-user"
                            }
                        },
                    )
                }
                resolve(comments)
            }, 2500)
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
                    text: text,
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
