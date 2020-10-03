import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Toilet} from "../services/ToiletService";
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
    toilet: Toilet
}

export default function ToiletPanel(props: ToiletPanelProps) {
    const classes = useStyles();
    const toilet = props.toilet;

    return (
        <ExpansionPanel>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
                <div className={classes.column}>
                    <Typography className={classes.heading}>
                        {toilet.title}
                    </Typography>
                </div>
                <div className={classes.column}>
                    <Box display="flex" width="100%" height="100%" justifyItems="center" alignItems="center">
                        <RatingView size="XS" rating={toilet.rating} />
                    </Box>
                </div>
                <div className={classes.column}>
                    <Typography className={classes.hintHeading}>
                        Distance: 0m
                    </Typography>
                </div>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
                <div className={classes.content}>
                    {
                        toilet.previewImage &&
                        <img src={toilet.previewImage} alt={toilet.id} width="100%"/>
                    }
                    <Typography variant="h3">
                        {props.toilet.title}
                    </Typography>
                    <Typography variant="h5">
                        {props.toilet.description}
                    </Typography>
                </div>
            </ExpansionPanelDetails>
            <ExpansionPanelActions>
                <Button size="small" color="primary">Rate</Button>
            </ExpansionPanelActions>
        </ExpansionPanel>
    )
}
