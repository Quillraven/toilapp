import {User} from "./User";
import {LocalDateTime} from "./LocalDateTime";

export interface ToiletComment {
    id: string,
    user: User,
    localDateTime: LocalDateTime,
    text: string,
}
