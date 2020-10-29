import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import React, {useEffect, useState} from "react";
import {Toilet} from "../model/Toilet";
import {ToiletComment} from "../model/ToiletComment"
import {RestToiletService, ToiletService} from "../services/ToiletService";
import TextField from "@material-ui/core/TextField"
import {Divider, GridList, GridListTile, IconButton, Snackbar, Typography} from "@material-ui/core";
import {Send} from "@material-ui/icons";
import {Alert, Color} from "@material-ui/lab";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        header: {
            h4: {
                display: "inline-block",
            }
        },
        addCommentForm: {
            '& > *': {
                marginLeft: theme.spacing(1),
                width: '25ch',
                display: "flex",
            },
        },
        commentGridListDiv: {
            display: "flex",
            flexWrap: "wrap",
            justifyContent: "space-around",
            overflow: "hidden",
            backgroundColor: theme.palette.background.paper,
        },
        commentGridList: {
            height: 200,
        },
        commentDiv: {
            whiteSpace: "pre-wrap"
        }
    }),
);

interface AlertState {
    text: string
    show: boolean
    severity: Color
}

interface CommentsProps {
    toilet: Toilet
}

export default function Comments(props: CommentsProps) {
    const classes = useStyles();
    const [comments, setComments] = useState<ToiletComment[]>([]);
    const [newCommentText, setNewCommentText] = useState<string>("")
    const [alert, setAlert] = useState<AlertState>({text: "", show: false, severity: "info"})
    const toiletService: ToiletService = new RestToiletService();

    const updateComment = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setNewCommentText(e.currentTarget.value)
    }

    const postComment = () => {
        // TODO retrieve correct user id
        toiletService
            .postComment(props.toilet.id, "5f9a860c37934c6ee3f49f6c", newCommentText)
            .then(response => {
                if (response) {
                    console.log("Comment posted")
                    setNewCommentText("")
                    setAlert({text: "Successfully posted comment!", show: true, severity: "success"})
                    return toiletService.getComments(props.toilet)
                }
                throw Error("Could not post comment")
            })
            .then(comments => {
                console.log(`New comments: ${comments.length}`)
                setComments(comments)
            })
            .catch((error) => {
                setAlert({text: "Could not post comment!", show: true, severity: "error"})
                console.error(`Error while posting comment: ${error}`)
            })
    };

    const closeAlert = () => {
        setAlert({text: "", show: false, severity: "info"})
    }

    useEffect(() => {
        toiletService
            .getComments(props.toilet)
            .then(comments => {
                console.log(`${comments.length} comments loaded`)
                setComments(comments)
            })
        // eslint-disable-next-line
    }, []);

    return (
        <React.Fragment>
            <div className={classes.header}>
                <h4>{comments.length} Comments</h4>
                <form className={classes.addCommentForm} noValidate autoComplete="off">
                    <TextField
                        id={"Comments-for-" + props.toilet.id}
                        placeholder="Add Comment"
                        value={newCommentText}
                        multiline
                        onChange={updateComment}
                        InputProps={{
                            endAdornment: (
                                <IconButton
                                    aria-label="send"
                                    onClick={postComment}
                                    disabled={!newCommentText}
                                    color="primary"
                                >
                                    <Send/>
                                </IconButton>
                            )
                        }}
                    />
                    <br/>
                </form>
            </div>
            <div>
                <GridList cellHeight="auto" className={classes.commentGridList} cols={1}>
                    {
                        comments.map((comment, idx) => (
                            <div className={classes.commentDiv} key={`Comment-${props.toilet.id}-${idx}`}>
                                <GridListTile>
                                    <Typography color="textSecondary">
                                        {comment.date.toLocaleTimeString(navigator.language, {
                                            year: "numeric",
                                            month: "numeric",
                                            day: "numeric",
                                            hour: "2-digit",
                                            minute: "2-digit"
                                        })}
                                    </Typography>
                                    <Typography variant="h5" component="h2" gutterBottom>
                                        {comment.user.name}
                                    </Typography>
                                    <Typography variant="body2" component="p">
                                        {comment.text}
                                    </Typography>
                                </GridListTile>
                                {idx !== comments.length - 1 && <Divider/>}
                            </div>
                        ))
                    }
                </GridList>
                <Snackbar open={alert.show} autoHideDuration={6000} onClose={closeAlert}>
                    <Alert severity={alert.severity} onClose={closeAlert}>
                        {alert.text}
                    </Alert>
                </Snackbar>
            </div>
        </React.Fragment>
    );
}
