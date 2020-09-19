import React from 'react';
import { Toilet, MockToiletService, ToiletService } from '../services/ToiletService';
import ToiletCard from './ToiletCard';
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';
import { GridList, GridListTile } from '@material-ui/core';

interface User {
    id: string
    name: string
    email: string
}

interface Comment {
    user: User
    date: Date
    text: string
}

interface ToiletListProps {
}

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
      width: 1000
    },
    icon: {
      color: 'rgba(255, 255, 255, 0.54)',
    },
  }),
);

export default function ToiletList() {
    const classes = useStyles();
    const [toilets, setToilets] = React.useState<Toilet[]>([]);
    const service: ToiletService = new MockToiletService();

    React.useEffect(() => {
        const loadToilets = async function() {
            console.log("load toilets");
            const toiletList: Toilet[] = await service.getToilets();
            console.log("toilets loaded");
            setToilets(toiletList);
        }
        loadToilets();
    }, []);

    return (
        <GridList cellHeight={350} className={classes.gridList} cols={3}>
            {toilets.map((toilet) => (
                <GridListTile key={toilet.title}>
                    <ToiletCard toilet={toilet} />
                </GridListTile>
            ))}
        </GridList>
    );
}


