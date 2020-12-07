import React, {useEffect, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {DefaultGeoLocationService, GeoLocationService} from '../services/GeoLocationService';
import {Container, Grid} from "@material-ui/core";
import {ToiletOverview} from "../model/ToiletOverview";
import {ToiletService, ToiletServiceProvider} from "../services/ToiletService";
import ToiletOverviewItem from "../components/ToiletOverviewItem";

const useStyles = makeStyles((_: Theme) =>
    createStyles({
        rootContainer: {
            paddingTop: 16,
            maxWidth: "lg",
        },
        gridItem: {
            display: "flex", // this line makes every grid item of the same height if the container is set to stretch
        },
    }),
);

export default function ToiletOverviewView() {
    const classes = useStyles();
    const [toiletOverviews, setToiletOverviews] = useState<ToiletOverview[]>([]);
    const toiletService: ToiletService = ToiletServiceProvider.getToiletService()
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
        <Container className={classes.rootContainer}>
            <Grid
                container
                direction="row"
                justify="space-evenly" // justify grid items itself
                alignItems="stretch" // this line together with display:"flex" in grid item makes all items of the same height
                spacing={2}
            >
                {
                    toiletOverviews.map(toiletOverview => (
                        <Grid item
                              className={classes.gridItem}
                              key={`GridItem-${toiletOverview.id}`} xs={12} sm={6} md={4} lg={3}
                        >
                            <ToiletOverviewItem toiletOverview={toiletOverview}/>
                        </Grid>
                    ))
                }
            </Grid>
        </Container>
    );
}
