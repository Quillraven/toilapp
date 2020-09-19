import React, {useEffect, useState} from 'react';
import {RestToiletService, Toilet, ToiletService} from '../services/ToiletService';
import ToiletCard from './ToiletCard';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {GridList, GridListTile} from '@material-ui/core';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'space-around',
            overflow: 'hidden',
            backgroundColor: theme.palette.background.paper,
        },
        gridList: {
            alignItems: "baseline",
            width: 1000
        },
        icon: {
            color: 'rgba(255, 255, 255, 0.54)',
        },
    }),
);

export default function ToiletList() {
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
        <GridList cellHeight={350} className={classes.gridList} cols={3}>
            {toilets.map((toilet) => (
                <GridListTile key={toilet.title}>
                    <ToiletCard toilet={toilet}/>
                </GridListTile>
            ))}
        </GridList>
    );
}


