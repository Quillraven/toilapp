import React, {useEffect, useState} from 'react';
import {RestToiletService, Toilet, ToiletService} from '../services/ToiletService';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import ToiletPanel from "./ToiletPanel";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            width: '25%',
        },
    }),
);

export default function ToiletOverview() {
    const classes = useStyles();
    const [toilets, setToilets] = useState<Toilet[]>([]);
    const service: ToiletService = new RestToiletService();

    useEffect(() => {
        service
            .getToilets()
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
                    <ToiletPanel toilet={toilet} key={toilet.id}/>
                ))
            }
        </div>
    );
}


