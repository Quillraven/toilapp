import {User} from "./User";

export interface Comment {
    id: string,
    user: User,
    date: Date,
    text: string,
}
