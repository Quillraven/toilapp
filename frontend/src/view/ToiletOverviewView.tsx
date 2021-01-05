import React, {createRef, useEffect, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {GeoLocationService, GeoLocationServiceProvider} from '../services/GeoLocationService';
import {Container, Fab, Grid, Grow, Snackbar} from "@material-ui/core";
import {ToiletOverview} from "../model/ToiletOverview";
import {ToiletService, ToiletServiceProvider} from "../services/ToiletService";
import ToiletOverviewItem from "../components/ToiletOverviewItem";
import {Add} from "@material-ui/icons";
import {ToiletDialog, ToiletDialogRef} from "../components/ToiletDialog";
import {Alert} from "@material-ui/lab";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        rootContainer: {
            paddingTop: 16,
            maxWidth: "lg",
        },
        gridItem: {
            display: "flex", // this line makes every grid item of the same height if the container is set to stretch
        },
        dialogButton: {
            position: "sticky",
            bottom: theme.spacing(2),
            marginRight: theme.spacing(2),
            left: "100%",
            ariaLabel: "add",
        },
    }),
);

export default function ToiletOverviewView() {
    const classes = useStyles();
    const toiletService: ToiletService = ToiletServiceProvider.getToiletService()
    const locationService: GeoLocationService = GeoLocationServiceProvider.getGeoLocationService()
    const [toiletOverviews, setToiletOverviews] = useState<ToiletOverview[]>([]);
    const [showAlert, setShowAlert] = useState(false)
    const dialogRef = createRef<ToiletDialogRef>()

    const closeAlert = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }

        setShowAlert(false)
    }

    useEffect(() => {
        (async () => {
            //TODO get maxDistanceInMeters from current user preferences
            const overviews = await toiletService.getToilets(locationService.getGeoLocation(), 4000000)
            console.log("Toilet data loaded")
            setToiletOverviews(overviews)
        })()
    }, [toiletService, locationService]);

    return (
        <Container className={classes.rootContainer}>
            <Grow
                in={true}
                style={{transformOrigin: '0 0 0'}}
                {...({timeout: 1000})}
            >
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
            </Grow>
            <ToiletDialog ref={dialogRef} title="Create new toilet" dispatchAlert={setShowAlert}/>
            <Fab className={classes.dialogButton} color="primary">
                <Add onClick={() => {
                    dialogRef.current?.handleOpen()
                }}/>
            </Fab>
            <Snackbar open={showAlert} autoHideDuration={6000} onClose={closeAlert}>
                <Alert severity="success" onClose={closeAlert}>
                    Successfully created toilet
                </Alert>
            </Snackbar>
        </Container>
    );
}
