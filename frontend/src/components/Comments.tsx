import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import React, {useEffect, useState} from "react";
import {ToiletComment} from "../model/ToiletComment"
import TextField from "@material-ui/core/TextField"
import {CircularProgress, Divider, GridList, GridListTile, IconButton, Snackbar, Typography} from "@material-ui/core";
import {Send} from "@material-ui/icons";
import {Alert, Color} from "@material-ui/lab";
import {ToiletDetails} from "../model/ToiletDetails";
import {CommentService, CommentServiceProvider} from "../services/CommentService";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        header: {
            display: "flex",
            flex: 1,
            flexDirection: "column",
        },
        addCommentForm: {
            '& > *': {
                marginLeft: theme.spacing(1),
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
        },
    }),
);

interface AlertState {
    text: string
    show: boolean
    severity: Color
}

interface CommentsProps {
    toiletDetails: ToiletDetails
}

export default function Comments(props: CommentsProps) {
    const classes = useStyles();
    const [comments, setComments] = useState<ToiletComment[]>([]);
    const [newCommentText, setNewCommentText] = useState<string>("")
    const [alert, setAlert] = useState<AlertState>({text: "", show: false, severity: "info"})
    const commentService: CommentService = CommentServiceProvider.getCommentService()
    const [isLoading, setIsLoading] = useState<boolean>(true)

    const updateComment = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setNewCommentText(e.currentTarget.value)
    }

    const postComment = () => {
        commentService
            .postComment(props.toiletDetails.id, newCommentText)
            .then(response => {
                if (response) {
                    console.log(`Comment ${JSON.stringify(response)} posted`)
                    setNewCommentText("")
                    setAlert({text: "Successfully posted comment!", show: true, severity: "success"})
                    setComments([response, ...comments])
                } else {
                    setAlert({text: "Could not post comment!", show: true, severity: "error"})
                }
            })
    };

    const closeAlert = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlert({text: "", show: false, severity: "info"})
    }

    useEffect(() => {
        if (props.toiletDetails.id) {
            setIsLoading(true)
            commentService
                //TODO get page, numComments depending on device
                .getComments(props.toiletDetails.id, 0, 10)
                .then(comments => {
                    console.log(`${comments.length} comments loaded`)
                    setComments(comments)
                    setIsLoading(false)
                })
        }
        // eslint-disable-next-line
    }, [props.toiletDetails]);

    return (
        <React.Fragment>
            <div className={classes.header}>
                <h4>{props.toiletDetails.numComments} Comments</h4>
                <Snackbar open={alert.show} autoHideDuration={6000} onClose={closeAlert}>
                    <Alert severity={alert.severity} onClose={closeAlert}>
                        {alert.text}
                    </Alert>
                </Snackbar>
                <form className={classes.addCommentForm} noValidate autoComplete="off">
                    <TextField
                        id={"Comments-for-" + props.toiletDetails.id}
                        placeholder="Add Comment"
                        value={newCommentText}
                        multiline
                        onChange={updateComment}
                        InputProps={{
                            startAdornment: (
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
                            <div className={classes.commentDiv} key={`Comment-${props.toiletDetails.id}-${idx}`}>
                                <GridListTile>
                                    <Typography color="textSecondary">
                                        {
                                            new Date(
                                                comment.localDateTime.year,
                                                comment.localDateTime.monthValue - 1,
                                                comment.localDateTime.dayOfMonth,
                                                comment.localDateTime.hour,
                                                comment.localDateTime.minute,
                                                comment.localDateTime.second
                                            ).toLocaleTimeString(navigator.language, {
                                                year: "numeric",
                                                month: "numeric",
                                                day: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit"
                                            })
                                        }
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
                    {isLoading &&
                    <GridListTile>
                        <CircularProgress/>
                        <Typography>
                            Loading comments...
                        </Typography>
                    </GridListTile>
                    }
                </GridList>
            </div>
        </React.Fragment>
    );
}
