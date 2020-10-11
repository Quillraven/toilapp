import React, {useEffect, useState} from 'react';
import {RestToiletService, ToiletResult, ToiletService} from '../services/ToiletService';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import ToiletPanel from "./ToiletPanel";
import { DefaultGeoLocationService, GeoLocationService } from '../services/GeoLocationService';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            width: '25%',
        },
    }),
);

export default function ToiletOverview() {
    const classes = useStyles();
    const [toilets, setToilets] = useState<ToiletResult[]>([]);
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
            {
                toilets.map(toilet => (
                    <ToiletPanel toilet={toilet} key={toilet.toilet.id}/>
                ))
            }
        </div>
    );
}


