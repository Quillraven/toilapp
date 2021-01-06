import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import React, {useEffect, useRef, useState} from "react";
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
        commentGridListDiv: {
            display: "flex",
            flexWrap: "wrap",
            justifyContent: "space-around",
            overflow: "hidden",
            backgroundColor: theme.palette.background.paper,
        },
        commentGridList: {
            maxHeight: 600,
        },
        commentDiv: {
            whiteSpace: "pre-wrap"
        },
        enterComment: {
            marginBottom: theme.spacing(2),
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
    const [numComments, setNumComments] = useState<number>(props.toiletDetails.numComments)
    const [alert, setAlert] = useState<AlertState>({text: "", show: false, severity: "info"})
    const commentService: CommentService = CommentServiceProvider.getCommentService()
    const [isLoading, setIsLoading] = useState<boolean>(true)
    const pageRef = useRef(0)
    // TODO get amount of comments to load from preferences/device type
    const numCommentsToLoad = 20

    const updateComment = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        setNewCommentText(e.currentTarget.value)
    }

    const postComment = async () => {
        try {
            const toiletComment = await commentService.postComment(props.toiletDetails.id, newCommentText)
            console.log(`Comment ${JSON.stringify(toiletComment)} posted`)
            setNewCommentText("")
            setAlert({text: "Successfully posted comment!", show: true, severity: "success"})
            setComments(prevComments => [toiletComment, ...prevComments])
            setNumComments(prevNumComments => prevNumComments + 1)
        } catch (error) {
            console.error(`Could not post comment: error=${error}`)
            setAlert({text: "Could not post comment!", show: true, severity: "error"})
        }
    };

    const onCommentsScroll = (event: React.SyntheticEvent) => {
        if (event.target instanceof Element) {
            const element: Element = event.target
            const scrollPercentage = element.scrollTop / (element.scrollHeight - element.clientHeight)

            if (scrollPercentage >= 0.7 && !isLoading && comments.length < numComments) {
                (async () => {
                    ++pageRef.current
                    console.log(`Loading comments for page ${pageRef.current}`)
                    setIsLoading(true)
                    try {
                        const toiletComments = await commentService.getComments(props.toiletDetails.id, pageRef.current, numCommentsToLoad)
                        setComments(
                            prevComments => {
                                const newValue = [...prevComments, ...toiletComments]
                                console.log(`${toiletComments.length} additional comments loaded. New size '${newValue.length}'`)
                                return newValue
                            }
                        )
                    } catch (error) {
                        console.error(`Could not load additional comments: error=${error}`)
                    }
                    setIsLoading(false)
                })()
            }
        }
    }

    const closeAlert = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setAlert(prevAlert => {
            return {...prevAlert, show: false}
        })
    }

    useEffect(() => {
        if (props.toiletDetails.id) {
            (async () => {
                setIsLoading(true)
                try {
                    const toiletComments = await commentService.getComments(props.toiletDetails.id, pageRef.current, numCommentsToLoad)
                    console.log(`${toiletComments.length} comments loaded for page '${pageRef.current}'`)
                    setComments(toiletComments)
                    setNumComments(props.toiletDetails.numComments)
                } catch (error) {
                    console.error(`Could not load comments: error=${error}`)
                    setNumComments(0)
                }
                setIsLoading(false)
            })()
        }
    }, [props.toiletDetails, commentService]);

    return (
        <React.Fragment>
            <div className={classes.header}>
                <h4>{numComments} Comments</h4>
                <Snackbar open={alert.show} autoHideDuration={6000} onClose={closeAlert}>
                    <Alert severity={alert.severity} onClose={closeAlert}>
                        {alert.text}
                    </Alert>
                </Snackbar>
                <TextField
                    className={classes.enterComment}
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
            </div>
            <div>
                <GridList cellHeight="auto" className={classes.commentGridList} cols={1} onScroll={onCommentsScroll}>
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
