import {User} from "./User";

export interface ToiletComment {
    id: string,
    user: User,
    date: Date,
    text: string,
}

export interface CreateUpdateComment {
    commentId: string,
    toiletId: string,
    text: string,
}
