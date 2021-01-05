import React, {useEffect, useState} from "react";
import {useLocation} from "react-router-dom"
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import ToiletDetailsItem from "../components/ToiletDetailsItem";
import {ToiletService, ToiletServiceProvider} from "../services/ToiletService";
import {GeoLocationService, GeoLocationServiceProvider} from "../services/GeoLocationService";
import {EMPTY_DETAILS, ToiletDetails} from "../model/ToiletDetails";

const useStyles = makeStyles((_: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
        },
    }),
);

interface LocationState {
    toiletId: string
}

export default function ToiletDetailsView() {
    const classes = useStyles();
    const [toiletDetails, setToiletDetails] = useState<ToiletDetails>(EMPTY_DETAILS)
    const location = useLocation<LocationState>()
    const locationService: GeoLocationService = GeoLocationServiceProvider.getGeoLocationService()
    const toiletService: ToiletService = ToiletServiceProvider.getToiletService()

    useEffect(() => {
        (async () => {
            const details = await toiletService.getToiletDetails(location.state.toiletId, locationService.getGeoLocation())
            console.log(`Toilet details loaded: ${JSON.stringify(details)}`)
            setToiletDetails(details)
        })()
    }, [toiletService, locationService, location.state.toiletId]);

    return (
        <div className={classes.root}>
            {<ToiletDetailsItem toiletDetails={toiletDetails}/>}
        </div>
    )
}
