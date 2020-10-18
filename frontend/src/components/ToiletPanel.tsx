import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {
    Button,
    ExpansionPanel,
    ExpansionPanelActions,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    Typography
} from "@material-ui/core";
import React from "react";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore"
import {RatingView} from "./Rating";
import {Toilet} from "../model/Toilet";
import Comments from "./Comments";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        heading: {
            fontSize: theme.typography.pxToRem(15),
        },
        secondaryHeading: {
            fontSize: theme.typography.pxToRem(15),
            color: theme.palette.text.secondary,
        },
        hintHeading: {
            fontSize: theme.typography.pxToRem(15),
            color: theme.palette.text.hint,
        },
        column: {
            flexBasis: '33.33%',
        },
        content: {
            flexBasis: '100%',
        },
    }),
);

interface ToiletPanelProps {
    toilet: Toilet
}

export default function ToiletPanel(props: ToiletPanelProps) {
    const classes = useStyles();
    const toilet = props.toilet;

    let distanceStr = toilet.distance.toFixed(0) + "m";
    if (toilet.distance >= 1000) {
        distanceStr = (toilet.distance / 1000).toFixed(1) + "km";
    } else if (toilet.distance < 0) {
        distanceStr = "-";
    }

    return (
        <ExpansionPanel>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                <div className={classes.column}>
                    <Typography className={classes.heading}>
                        {toilet.title}
                    </Typography>
                </div>
                <div className={classes.column}>
                    <Typography className={classes.hintHeading}>
                        <RatingView size="XS" rating={toilet.rating}/>
                    </Typography>
                </div>
                <div className={classes.column}>
                    <Typography className={classes.hintHeading}>
                        {distanceStr}
                    </Typography>
                </div>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
                <div className={classes.content}>
                    {
                        toilet.previewURL &&
                        <img src={toilet.previewURL} alt={toilet.id} width="100%"/>
                    }
                    <Typography variant="h3">
                        {props.toilet.title}
                    </Typography>
                    <Typography variant="h5">
                        {props.toilet.description}
                    </Typography>

                    <Typography variant="button">
                        <Comments toilet={toilet}/>
                    </Typography>
                </div>
            </ExpansionPanelDetails>
            <ExpansionPanelActions>
                <Button size="small" color="primary">Rate</Button>
            </ExpansionPanelActions>
        </ExpansionPanel>
    )
}
