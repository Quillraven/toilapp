import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {ToiletResult} from "../services/ToiletService";
import {
    Box,
    Button,
    ExpansionPanel,
    ExpansionPanelActions,
    ExpansionPanelDetails,
    ExpansionPanelSummary,
    Typography
} from "@material-ui/core";
import React from "react";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore"
import { RatingView } from "./Rating";

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
    toilet: ToiletResult
}

export default function ToiletPanel(props: ToiletPanelProps) {
    const classes = useStyles();
    const toilet = props.toilet;
    let distanceStr = toilet.distance.toFixed(0) + "m";
    if(toilet.distance >= 1000) {
        distanceStr = (toilet.distance / 1000).toFixed(1) + "km";
    } else if(toilet.distance < 0) {
        distanceStr = "-";
    }
    return (
        <ExpansionPanel>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                <div className={classes.column}>
                    <Typography className={classes.heading}>
                        {toilet.toilet.title}
                    </Typography>
                </div>
                <div className={classes.column}>
                    <Box display="flex" width="100%" height="100%" justifyItems="center" alignItems="center">
                        <RatingView size="XS" rating={toilet.toilet.rating} />
                    </Box>
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
                        toilet.toilet.previewImage &&
                        <img src={toilet.toilet.previewImage} alt={toilet.toilet.id} width="100%"/>
                    }
                    <Typography variant="h3">
                        {props.toilet.toilet.title}
                    </Typography>
                    <Typography variant="h5">
                        {props.toilet.toilet.description}
                    </Typography>
                </div>
            </ExpansionPanelDetails>
            <ExpansionPanelActions>
                <Button size="small" color="primary">Rate</Button>
            </ExpansionPanelActions>
        </ExpansionPanel>
    )
}
