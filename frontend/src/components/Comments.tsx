import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import React, {useEffect, useState} from "react";
import {Toilet} from "../model/Toilet";
import {ToiletComment} from "../model/ToiletComment"
import {RestToiletService, ToiletService} from "../services/ToiletService";
import {
    ExpansionPanel,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    List,
    ListItem,
    ListItemText,
    Typography
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            width: '100%',
            maxWidth: '36ch',
            backgroundColor: theme.palette.background.paper,
        },
        inline: {
            display: 'inline',
        },
    }),
);

interface CommentsProps {
    toilet: Toilet
}

export default function Comments(props: CommentsProps) {
    const classes = useStyles();
    const [comments, setComments] = useState<ToiletComment[]>([]);
    const toiletService: ToiletService = new RestToiletService();

    useEffect(() => {
        toiletService
            .getComments(props.toilet)
            .then(comments => {
                console.log("Comments loaded")
                setComments(comments)
            })
        // eslint-disable-next-line
    }, []);

    return (
        <ExpansionPanel>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                <div>
                    <Typography>
                        View Comments
                    </Typography>
                </div>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
                <List className={classes.root}>
                    {
                        comments.map((comment, idx) => (
                            <ListItem alignItems="flex-start" key={`Comment-${idx}`}>
                                <ListItemText
                                    primary={comment.user.name}
                                    secondary={
                                        <React.Fragment>
                                            <Typography
                                                component="span"
                                                variant="body1"
                                                className={classes.inline}
                                                color="textPrimary"
                                            >
                                                {comment.date.toLocaleString()}
                                            </Typography>
                                            <br/>
                                            <Typography
                                                component="span"
                                                variant="body2"
                                            >
                                                {comment.text}
                                            </Typography>
                                        </React.Fragment>
                                    }
                                />
                            </ListItem>
                        ))
                    }
                </List>
            </ExpansionPanelDetails>
        </ExpansionPanel>
    );
}
