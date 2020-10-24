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
            padding: "20px"
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
                direction="row"
                justify="center"
                alignItems="center"
                spacing={3}>
                {
                    toilets.map(toilet => (
                        <Grid item key={`GridItem-${toilet.id}`} style={{width: "400px"}}>
                            <ToiletOverviewItem toilet={toilet} key={`OverviewItem-${toilet.id}`}/>
                        </Grid>
                    ))
                }
            </Grid>
        </div>
    );
}
