import React, {useEffect, useState} from "react";
import {useLocation} from "react-router-dom"
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import ToiletDetailsItem from "../components/ToiletDetailsItem";
import {ToiletService, ToiletServiceProvider} from "../services/ToiletService";
import {DefaultGeoLocationService, GeoLocationService} from "../services/GeoLocationService";
import {EMPTY_DETAILS, ToiletDetails} from "../model/ToiletDetails";

const useStyles = makeStyles((theme: Theme) =>
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
    const locationService: GeoLocationService = new DefaultGeoLocationService()
    const toiletService: ToiletService = ToiletServiceProvider.getToiletService()

    useEffect(() => {
        toiletService
            .getToiletDetails(location.state.toiletId, locationService.getGeoLocation())
            .then(toiletDetails => {
                console.log(`Toilet details loaded: ${JSON.stringify(toiletDetails)}`)
                setToiletDetails(toiletDetails)
            })
        // eslint-disable-next-line
    }, []);

    return (
        <div className={classes.root}>
            {<ToiletDetailsItem toiletDetails={toiletDetails}/>}
        </div>
    )
}
