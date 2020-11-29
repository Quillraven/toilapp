import React, {useEffect, useState} from 'react';
import {RestToiletService, ToiletService} from '../services/ToiletService';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import ToiletOverviewItem from "../components/ToiletOverviewItem";
import {DefaultGeoLocationService, GeoLocationService} from '../services/GeoLocationService';
import {Grid} from "@material-ui/core";
import {ToiletOverview} from "../model/ToiletOverview";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
            padding: "20px"
        },
    }),
);

export default function ToiletOverviewView() {
    const classes = useStyles();
    const [toiletOverviews, setToiletOverviews] = useState<ToiletOverview[]>([]);
    const toiletService: ToiletService = new RestToiletService();
    const locationService: GeoLocationService = new DefaultGeoLocationService();

    useEffect(() => {
        toiletService
            //TODO get maxDistanceInMeters from current user preferences
            .getToilets(locationService.getGeoLocation(), 4000000)
            .then(toiletOverviews => {
                console.log("Toilet data loaded")
                setToiletOverviews(toiletOverviews)
            })
        // eslint-disable-next-line
    }, []);

    return (
        <div className={classes.root}>
            <Grid
                container
                direction="row"
                justify="center"
                alignItems="center"
                spacing={3}>
                {
                    toiletOverviews.map(toiletOverview => (
                        <Grid item key={`GridItem-${toiletOverview.id}`} style={{width: "400px"}}>
                            <ToiletOverviewItem toiletOverview={toiletOverview}
                                                key={`OverviewItem-${toiletOverview.id}`}/>
                        </Grid>
                    ))
                }
            </Grid>
        </div>
    );
}
