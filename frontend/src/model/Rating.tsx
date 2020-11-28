import {User} from "./User";

export interface Rating {
    id: string,
    user: User,
    value: number,
}
