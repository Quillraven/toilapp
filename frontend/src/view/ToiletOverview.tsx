import React, {useEffect, useState} from 'react';
import {RestToiletService, ToiletService} from '../services/ToiletService';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import ToiletOverviewItem from "../components/ToiletOverviewItem";
import {DefaultGeoLocationService, GeoLocationService} from '../services/GeoLocationService';
import {Toilet} from "../model/Toilet";
import {Grid} from "@material-ui/core";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
        },
    }),
);

export default function ToiletOverview() {
    const classes = useStyles();
    const [toilets, setToilets] = useState<Toilet[]>([]);
    const toiletService: ToiletService = new RestToiletService();
    const locationService: GeoLocationService = new DefaultGeoLocationService();

    useEffect(() => {
        toiletService
            .getToilets(locationService.getGeoLocation())
            .then(toilets => {
                console.log("Toilet data loaded")
                setToilets(toilets)
            })
        // eslint-disable-next-line
    }, []);

    return (
        <div className={classes.root}>
            <Grid
                container
                direction="column"
                justify="center"
                alignItems="center">
                {
                    toilets.map(toilet => (
                        <Grid item xs key={`GridItem-${toilet.id}`}>
                            <ToiletOverviewItem toilet={toilet} key={`OverviewItem-${toilet.id}`}/>
                        </Grid>
                    ))
                }
            </Grid>
        </div>
    );
}
