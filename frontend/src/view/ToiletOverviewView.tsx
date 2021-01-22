import React, {createRef, useEffect, useRef, useState} from 'react';
import {createStyles, makeStyles, Theme} from '@material-ui/core/styles';
import {GeoLocationService, GeoLocationServiceProvider} from '../services/GeoLocationService';
import {CircularProgress, debounce, Fab, Grid, Grow, Snackbar, Typography} from "@material-ui/core";
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
            margin: theme.spacing(2),
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
    const [isLoading, setIsLoading] = useState(false)
    const dialogRef = createRef<ToiletDialogRef>()
    const minDistance = useRef(0)
    const idsToExclude = useRef<String[]>([])
    const hasMoreToilets = useRef(true)

    const closeAlert = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }

        setShowAlert(false)
    }

    const updateOverviewInformation = (sortedLastOverviews: ToiletOverview[]) => {
        if (!sortedLastOverviews || sortedLastOverviews.length <= 0) {
            // did not retrieve any toilets from server -> assume there are no more toilets to load
            minDistance.current = 0
            idsToExclude.current = []
            hasMoreToilets.current = false
        } else {
            // use distance of last toilet for filtering for next get call and also
            // exclude all toilets with the same distance
            const lastOverview = sortedLastOverviews[sortedLastOverviews.length - 1]
            minDistance.current = lastOverview.distance
            setToiletOverviews(prevOverviews => {
                const newValue = [...prevOverviews, ...sortedLastOverviews]
                idsToExclude.current = newValue
                    .filter(it => it.distance === minDistance.current)
                    .map(it => it.id)
                return newValue
            })
        }

        console.log(`minDistance='${minDistance.current}', idsToExclude='${idsToExclude.current}, hasMoreToilets='${hasMoreToilets.current}'`)
    }

    useEffect(
        () => {
            function loadToilets() {
                (async () => {
                    if (isLoading || !hasMoreToilets) {
                        // do not run this method if a load is still in progress
                        // or if there is nothing else to fetch anymore
                        return
                    }

                    setIsLoading(true)
                    try {
                        //TODO get maxDistanceInMeters and toiletsToLoad from current user preferences
                        const overviews = await toiletService.getToilets(
                            locationService.getGeoLocation(),
                            4000,
                            20,
                            minDistance.current,
                            idsToExclude.current
                        )
                        console.log("Toilet data loaded")
                        updateOverviewInformation(overviews)
                    } catch (error) {
                        console.error(`Error while loading toilet data: ${error}`)
                    }
                    setIsLoading(false)
                })()
            }

            const onToiletsScroll = debounce(() => {
                const element: Element = document.documentElement
                const scrollPercentage = element.scrollTop / (element.scrollHeight - element.clientHeight)

                if (scrollPercentage >= 0.5 && !isLoading && hasMoreToilets.current) {
                    loadToilets()
                }
            })

            if (toiletOverviews.length === 0) {
                // make initial call to loadToilets only if there are no toilets loaded yet
                loadToilets()
            }

            window.addEventListener("scroll", onToiletsScroll)
            return () => {
                window.removeEventListener("scroll", onToiletsScroll)
            }
        },
        [locationService, toiletService, isLoading, toiletOverviews]
    );

    return (
        <div className={classes.rootContainer}>
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
                                  key={`GridItem-${toiletOverview.id}`} xs={12} sm={6} md={4} lg={3} xl={2}
                            >
                                <ToiletOverviewItem toiletOverview={toiletOverview}/>
                            </Grid>
                        ))
                    }
                </Grid>
            </Grow>
            {isLoading &&
            <div>
                <CircularProgress/>
                <Typography>
                    Loading nearby toilets...
                </Typography>
            </div>
            }
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
        </div>
    );
}
