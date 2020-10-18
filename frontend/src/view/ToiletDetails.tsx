import React from "react";
import {useLocation} from "react-router-dom"
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import {Toilet} from "../model/Toilet";
import ToiletDetailsItem from "../components/ToiletDetailsItem";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
        },
    }),
);

interface ToiletDetailsProps {
    toiletId: string
}

interface LocationState {
    toilet: Toilet
}

export default function ToiletDetails() {
    const classes = useStyles();
    const location = useLocation<LocationState>()

    return (
        <div className={classes.root}>
            {<ToiletDetailsItem toilet={location.state.toilet}/>}
        </div>
    )
}
